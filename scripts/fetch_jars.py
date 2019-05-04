import sys
import requests
import json
import subprocess

PATH = "path"
NAME = "name"
GITHUB_API= "https://api.github.com"
LOGIN = "login"
DOWNLOAD_URL='browser_download_url'
ASSETS="assets"

def fetchJars():
    inputPath = sys.argv[1] # input path passed as cli argument
    outputPath = sys.argv[2] # output path passed as cli argument
    token = sys.argv[3] # token passed as cli argument
    f = open(inputPath, "r")
    file = f.read()
    f.close()

    bruteLines = file.split("\n")
    parsed = parse_output(bruteLines)
    tokenUser = get_github_user(token)[LOGIN]
    for project in parsed:
        splitedProjectPath = project[PATH].split('/')
        projectName = splitedProjectPath[len(splitedProjectPath) - 1]
        githubProject = tokenUser + '/' + projectName
        releases = get_github_releases(token, githubProject)
        for release in releases:
            if (release[NAME].startswith("fetchjar-")):
                commitSHA = release[NAME].replace("fetchjar-", '')
                downloadPath = mount_download_path(outputPath, project, commitSHA)
                downloadUrl = release[ASSETS][0][DOWNLOAD_URL]
                print downloadPath + 'is ready'
                download_file(downloadUrl, downloadPath)
                untar_and_remove_file(downloadPath)

def parse_output(lines):
    result = []
    for line in lines[1:]:
        cells = line.split(",")
        if (len (cells) > 1):
            method = {}
            method[NAME] = cells[0]
            method[PATH] = cells[1]
            result.append(method)
    return result

def download_file(url, target_path):
    response = requests.get(url, stream=True)
    if response.status_code == 200:
        with open(target_path, 'wb') as f:
            f.write(response.raw.read())

def mount_download_path(outputPath, project, commitSHA):
    return outputPath + '/files/' + project[NAME] + '/' + commitSHA + '/result.tar.gz'

def untar_and_remove_file(downloadPath):
    downloadDir = downloadPath.replace('result.tar.gz', '')
    subprocess.call(['mkdir', downloadDir + 'build'])
    subprocess.call(['tar', '-xf', downloadPath, '-C', downloadDir + '/build', ])
    subprocess.call(['rm', downloadPath])

def get_github_user(token):
    return requests.get(GITHUB_API + '/user', headers=get_headers(token)).json()

def get_github_releases(token, project):
    return requests.get(GITHUB_API + '/repos/' + project + '/releases', headers=get_headers(token)).json()

def get_headers(token):
    return {
        "Authorization": "token " + token
    }

fetchJars()