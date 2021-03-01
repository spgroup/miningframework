import requests
import time

GITHUB_API= "https://api.github.com"

STATUS = "status"
SUCCESS = "success"
BRANCH = "branch"

def wait_for_builds(project):
    has_pendent = True
    filtered_builds = []
    while (has_pendent):
        builds = get_github_builds(project)
        
        has_pendent = False
        for build in builds:
            has_pendent = has_pendent or (build[STATUS] != SUCCESS)
    
        if (has_pendent):
            print ("Waiting 30 seconds")
            time.sleep(30)
        else:
            for build in filtered_builds:
                print (build[BRANCH] + ": " + build[STATUS])

    return filtered_builds


def get_github_builds(project):
    res = requests.get(f"{GITHUB_API}/repos/{project}/actions/runs")

    try: 
        res.raise_for_status()

        return res.json()
    except Exception as e:
        raise Exception("Error getting github actions builds: " + str(e))