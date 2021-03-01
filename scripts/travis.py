import requests

BRANCH = "branch"
STATE = "state"
FINISHED = "finished"
UNTAGGED = "untagged"

TRAVIS_API = "https://api.travis-ci.org"

def wait_for_builds(project):
    has_pendent = True
    filtered_builds = []
    while (has_pendent):
        builds = get_travis_project_builds(project)
        filtered_builds = filter (lambda x: not x[BRANCH].startswith(UNTAGGED), builds)
        
        has_pendent = False
        for build in filtered_builds:
            has_pendent = has_pendent or (build[STATE] != FINISHED)
    
        if (has_pendent):
            print ("Waiting 30 seconds")
            time.sleep(30)
        else:
            for build in filtered_builds:
                print (build[BRANCH] + ": " + build[STATE] )

    return filtered_builds


def get_travis_project_builds(project):
    res = requests.get(TRAVIS_API + '/repos/' + project + '/builds')

    try: 
        res.raise_for_status()

        return res.json()
    except Exception as e:
        raise Exception("Error getting travis builds: " + str(e))