import requests
import time

GITHUB_API= "https://api.github.com"

WORKFLOW_RUNS = "workflow_runs"
TOTAL_COUNT = "total_count"
CONCLUSION = "conclusion"

def wait_for_builds(project):
    has_pendent = True
    filtered_builds = []
    while (has_pendent):
        res = get_github_in_progress_builds(project)
        builds = res[WORKFLOW_RUNS]

        has_pendent = False
        for build in builds:
            has_pendent = has_pendent or (build[CONCLUSION] == None)

        if (has_pendent):
            print ("Waiting 30 seconds")
            time.sleep(30)

    return filtered_builds


def get_github_in_progress_builds(project):
    res = requests.get(f"{GITHUB_API}/repos/{project}/actions/runs", params={'per_page': 100})

    try: 
        res.raise_for_status()

        return res.json()
    except Exception as e:
        raise Exception("Error getting github actions builds: " + str(e))