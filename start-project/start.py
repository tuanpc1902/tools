import concurrent.futures
import json
import os
import re
import shutil
import subprocess
import sys
import time
from urllib.parse import urlparse

CONFIG_PATH = os.path.join(os.path.dirname(__file__), "config.json")


def load_config(path):
    with open(path, "r", encoding="ascii") as f:
        return json.load(f)


def ensure_dir(path):
    os.makedirs(path, exist_ok=True)


def normalize_gitlab_host(base_url):
    parsed = urlparse(base_url)
    if parsed.hostname:
        return parsed.hostname
    return base_url.replace("https://", "").replace("http://", "").strip("/")


def build_https_url(base_url, namespace, name, username, token):
    parsed = urlparse(base_url)
    host = parsed.netloc or normalize_gitlab_host(base_url)
    scheme = parsed.scheme or "https"
    auth = ""
    if username and token:
        auth = f"{username}:{token}@"
    if namespace:
        return f"{scheme}://{auth}{host}/{namespace}/{name}.git"
    return f"{scheme}://{auth}{host}/{name}.git"


def gitlab_clone_url(repo, gitlab):
    override = repo.get("clone_url", "").strip()
    if override:
        return override

    base_url = gitlab.get("base_url", "").strip().rstrip("/")
    namespace = gitlab.get("namespace", "").strip().strip("/")
    name = repo.get("name", "").strip()

    if not base_url or not name:
        return ""

    if gitlab.get("use_ssh", False):
        host = normalize_gitlab_host(base_url)
        if namespace:
            return f"git@{host}:{namespace}/{name}.git"
        return f"git@{host}:{name}.git"

    username = gitlab.get("https_username", "").strip()
    token = gitlab.get("https_token", "").strip()
    return build_https_url(base_url, namespace, name, username, token)


def repo_path(repo, destination_root):
    override = repo.get("path", "").strip()
    if override:
        return override
    return os.path.join(destination_root, repo.get("name", "").strip())


def run_command(args, cwd=None):
    try:
        return subprocess.run(args, cwd=cwd, check=False).returncode
    except FileNotFoundError:
        return 1


def run_shell_command(command, cwd=None):
    try:
        return subprocess.run(["cmd", "/c", command], cwd=cwd, check=False).returncode
    except FileNotFoundError:
        return 1


def clone_or_update_repo(repo, gitlab, destination_root, update_existing):
    url = gitlab_clone_url(repo, gitlab)
    if not url:
        return False, "Missing git url"

    target_path = repo_path(repo, destination_root)
    if os.path.isdir(os.path.join(target_path, ".git")):
        if update_existing:
            code = run_command(["git", "-C", target_path, "pull"])
            if code == 0:
                return True, "Updated"
            return False, "Git pull failed"
        return True, "Exists"

    ensure_dir(os.path.dirname(target_path))
    code = run_command(["git", "clone", url, target_path])
    if code == 0:
        return True, "Cloned"
    return False, "Git clone failed"


def is_nginx_running():
    try:
        output = subprocess.check_output(["tasklist"], text=True, errors="ignore")
    except Exception:
        return False
    return "nginx.exe" in output.lower()


def get_local_ipv4():
    try:
        output = subprocess.check_output(["ipconfig"], text=True, errors="ignore")
        matches = re.findall(r"\b(?:\d{1,3}\.){3}\d{1,3}\b", output)
        for ip in matches:
            if not ip.startswith(("127.", "169.254.", "0.")):
                return ip
    except Exception:
        pass
    return ""


def update_nginx_conf(conf_path, replace_ip, local_ip):
    if not conf_path or not os.path.exists(conf_path):
        return False, "nginx.conf not found"
    if not replace_ip or not local_ip:
        return False, "Missing IP"

    with open(conf_path, "r", encoding="ascii", errors="ignore") as f:
        content = f.read()

    if replace_ip not in content:
        return False, "Replace IP not found"

    content = content.replace(replace_ip, local_ip)
    with open(conf_path, "w", encoding="ascii") as f:
        f.write(content)
    return True, "nginx.conf updated"


def find_latest_artifact(path, override_path):
    if override_path:
        candidate = os.path.join(path, override_path)
        if os.path.isfile(candidate):
            return candidate
        return ""

    target_dir = os.path.join(path, "target")
    if not os.path.isdir(target_dir):
        return ""

    newest = None
    newest_mtime = 0
    for root, _, files in os.walk(target_dir):
        for name in files:
            if name.endswith((".jar", ".war")):
                full = os.path.join(root, name)
                mtime = os.path.getmtime(full)
                if mtime > newest_mtime:
                    newest = full
                    newest_mtime = mtime
    return newest or ""


def build_backend(repo, path):
    install_cmd = repo.get("install_command", "").strip() or "mvn clean install"
    code = run_shell_command(install_cmd, cwd=path)
    if code != 0:
        return False, "mvn build failed", ""

    artifact = find_latest_artifact(path, repo.get("artifact_path", "").strip())
    if not artifact:
        return False, "No jar/war found", ""
    return True, "Built", artifact


def build_frontend(repo, path):
    install_cmd = repo.get("install_command", "").strip() or "npm i"
    code = run_shell_command(install_cmd, cwd=path)
    if code != 0:
        return False, "npm install failed", ""
    return True, "Installed", ""


def build_custom(repo, path):
    build_cmd = repo.get("build_command", "").strip()
    if not build_cmd:
        return True, "Skipped", ""
    code = run_shell_command(build_cmd, cwd=path)
    if code != 0:
        return False, "build failed", ""
    return True, "Built", ""


def ps_escape(value):
    return value.replace("'", "''")


def build_run_ps(repo, path, artifact_path):
    repo_type = repo.get("type", "").strip().lower() or "custom"
    target_path = ps_escape(path)

    if repo_type == "backend":
        run_cmd = repo.get("run_command", "").strip()
        if run_cmd:
            return f"$ErrorActionPreference='Stop'; Set-Location -Path '{target_path}'; {run_cmd}"
        if artifact_path:
            return (
                f"$ErrorActionPreference='Stop'; Set-Location -Path '{target_path}'; "
                f"java -jar '{ps_escape(artifact_path)}'"
            )
        return ""

    if repo_type == "frontend":
        run_cmd = repo.get("run_command", "").strip() or "npm start"
        return f"$ErrorActionPreference='Stop'; Set-Location -Path '{target_path}'; {run_cmd}"

    run_cmd = repo.get("command", "").strip()
    if not run_cmd:
        return ""
    return f"$ErrorActionPreference='Stop'; Set-Location -Path '{target_path}'; {run_cmd}"


def run_in_windows_terminal(title, ps_command, wt_path):
    if not ps_command:
        return False, "Missing command"

    args = [
        wt_path,
        "-w",
        "0",
        "new-tab",
        "--title",
        title,
        "powershell",
        "-NoExit",
        "-Command",
        ps_command,
    ]
    try:
        subprocess.run(args, check=False)
        return True, "Launched"
    except FileNotFoundError:
        return False, "Windows Terminal (wt.exe) not found"


def run_repo(repo, destination_root, wt_path, artifact_path):
    path = repo_path(repo, destination_root)
    ps_command = build_run_ps(repo, path, artifact_path)
    return run_in_windows_terminal(repo.get("name", "App"), ps_command, wt_path)


def build_repo(repo, destination_root):
    path = repo_path(repo, destination_root)
    repo_type = repo.get("type", "").strip().lower() or "custom"

    if repo_type == "backend":
        return build_backend(repo, path)
    if repo_type == "frontend":
        return build_frontend(repo, path)
    return build_custom(repo, path)


def clone_repos_parallel(repos, gitlab, destination_root, update_existing, max_workers, throttle_ms):
    results = []
    if max_workers <= 1:
        for repo in repos:
            results.append((repo, *clone_or_update_repo(repo, gitlab, destination_root, update_existing)))
        return results

    with concurrent.futures.ThreadPoolExecutor(max_workers=max_workers) as executor:
        future_map = {}
        for repo in repos:
            future = executor.submit(
                clone_or_update_repo, repo, gitlab, destination_root, update_existing
            )
            future_map[future] = repo
            if throttle_ms > 0:
                time.sleep(throttle_ms / 1000.0)

        for future in concurrent.futures.as_completed(future_map):
            repo = future_map[future]
            try:
                ok, msg = future.result()
            except Exception:
                ok, msg = False, "Clone failed"
            results.append((repo, ok, msg))

    return results


def build_repos_parallel(repos, destination_root, max_workers, throttle_ms):
    results = []
    if max_workers <= 1:
        for repo in repos:
            ok, msg, artifact = build_repo(repo, destination_root)
            results.append((repo, ok, msg, artifact))
        return results

    with concurrent.futures.ThreadPoolExecutor(max_workers=max_workers) as executor:
        future_map = {}
        for repo in repos:
            future = executor.submit(build_repo, repo, destination_root)
            future_map[future] = repo
            if throttle_ms > 0:
                time.sleep(throttle_ms / 1000.0)

        for future in concurrent.futures.as_completed(future_map):
            repo = future_map[future]
            try:
                ok, msg, artifact = future.result()
            except Exception:
                ok, msg, artifact = False, "Build failed", ""
            results.append((repo, ok, msg, artifact))

    return results


def main():
    if not os.path.exists(CONFIG_PATH):
        print(f"Missing config: {CONFIG_PATH}")
        sys.exit(1)

    data = load_config(CONFIG_PATH)
    repos = data.get("repos", [])
    gitlab = data.get("gitlab", {})
    destination_root = data.get("destination_root", "").strip()
    update_existing = bool(data.get("update_existing", False))
    parallel_cfg = data.get("parallel", {})
    nginx_cfg = data.get("nginx", {})

    if not isinstance(repos, list) or not repos:
        print("Config has no repos")
        sys.exit(1)

    if not destination_root:
        print("Missing destination_root")
        sys.exit(1)

    enabled_repos = [r for r in repos if bool(r.get("enable", False))]
    if not enabled_repos:
        print("No enabled repos")
        sys.exit(0)

    clone_workers = int(parallel_cfg.get("clone_max_workers", 1))
    build_workers = int(parallel_cfg.get("build_max_workers", 1))
    throttle_ms = int(parallel_cfg.get("throttle_ms", 0))

    print("\nCloning repositories...")
    clone_results = clone_repos_parallel(
        enabled_repos, gitlab, destination_root, update_existing, clone_workers, throttle_ms
    )
    for repo, ok, msg in clone_results:
        print(f"- {repo.get('name', 'repo')}: {msg}")

    print("\nBuilding repositories...")
    build_results = build_repos_parallel(enabled_repos, destination_root, build_workers, throttle_ms)
    build_map = {}
    for repo, ok, msg, artifact in build_results:
        build_map[repo.get("name", "repo")] = (ok, artifact)
        print(f"- {repo.get('name', 'repo')}: {msg}")

    wt_path = shutil.which("wt") or "wt"

    print("\nStarting apps in Windows Terminal...")
    for repo in enabled_repos:
        name = repo.get("name", "repo")
        ok, artifact = build_map.get(name, (True, ""))
        if not ok:
            print(f"- {name}: skipped (build failed)")
            continue
        ok, msg = run_repo(repo, destination_root, wt_path, artifact)
        print(f"- {name}: {msg}")

    conf_path = nginx_cfg.get("conf_path", "").strip()
    replace_ip = nginx_cfg.get("replace_ip", "").strip()
    auto_restart = bool(nginx_cfg.get("auto_restart", False))
    restart_command = nginx_cfg.get("restart_command", "").strip()

    if conf_path and replace_ip:
        if is_nginx_running():
            print("\nnginx is running, skip config update")
        else:
            local_ip = get_local_ipv4()
            updated, msg = update_nginx_conf(conf_path, replace_ip, local_ip)
            print(f"\nnginx config: {msg}")
            if updated and auto_restart and restart_command:
                code = run_shell_command(restart_command)
                if code == 0:
                    print("nginx restart: OK")
                else:
                    print("nginx restart: failed")


if __name__ == "__main__":
    main()
