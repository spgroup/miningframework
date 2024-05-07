import requests
import re
import csv
import argparse

# Lista para armazenar os dados de todos os projetos
all_project_data = []

def commitCount(u, r, token):
    headers = {'Authorization': 'token ' + token}
    return re.search('\d+$', requests.get('https://api.github.com/repos/{}/{}/commits?per_page=1'.format(u, r), headers=headers).links['last']['url']).group()

def commitDevs(u, r, token):
    headers = {'Authorization': 'token ' + token}
    return re.search('\d+$', requests.get('https://api.github.com/repos/{}/{}/contributors?per_page=1&anon=true'.format(u, r), headers=headers).links['last']['url']).group()

def get_repository_data(owner, repo, token):
    base_url = 'https://api.github.com/repos/{}/{}'.format(owner, repo)
    headers = {'Authorization': 'token ' + token}

    response = requests.get(base_url, headers=headers)
    data = response.json()

    organization = data['owner']['login']
    project_name = data['name']
    loc = data['size']
    forks = data['forks_count']
    stars = data['stargazers_count']

    developers = commitDevs(owner, repo, token)
    commits_count = commitCount(owner, repo, token)

    return organization, project_name, loc, forks, stars, commits_count, developers

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Get GitHub repository data.')
    parser.add_argument('--token', help='GitHub Access Token', required=True)
    args = parser.parse_args()

    projects = [['brettwooldridge', 'HikariCP'], ['PhilJay', 'MPAndroidChart'], ['opentripplanner', 'OpenTripPlanner'], ['ReactiveX', 'RxJava'], ['Netflix', 'SimianArmy'], ['Activiti', 'activiti'], ['Alluxio', 'alluxio'], ['antlr', 'antlr4'], ['CloudSlang', 'cloud-slang'], ['yasserg', 'crawler4j'], ['cucumber', 'cucumber-jvm'], ['dropwizard', 'dropwizard'], ['apache', 'druid'], ['elastic', 'elasticsearch'], ['richardwilly98', 'elasticsearch-river-mongodb'], ['unclebob', 'fitnesse'], ['hector-client', 'hector'], ['DiUS', 'java-faker'], ['jenkinsci', 'jenkins'], ['jhy', 'jsoup'], ['libgdx', 'libgdx'], ['qos-ch', 'logback'], ['netty', 'netty'], ['square', 'okhttp'], ['orientechnologies', 'orientdb'], ['jchambers', 'pushy'], ['sanity', 'quickml'], ['resty-gwt', 'resty-gwt'], ['square', 'retrofit'], ['spring-projects', 'spring-boot'], ['apache', 'storm'], ['swagger-api', 'swagger-core'], ['kongchen', 'swagger-maven-plugin'], ['thinkaurelius', 'titan'], ['vavr-io', 'vavr'], ['voldemort', 'voldemort'], ['webbit', 'webbit']]

    for project in projects:
        organization, project_name, loc, forks, stars, commits_count, developers = get_repository_data(project[0], project[1], args.token)
        print(f'Organização/Projeto: {organization}/{project_name}')
        print(f'Linhas de Código: {loc}')
        print(f'Forks: {forks}')
        print(f'Stars: {stars}')
        print(f'Número de Commits: {commits_count}')
        print(f'Número de Desenvolvedores: {developers}')
        # Armazenar os dados em uma lista
        project_data = [organization, project_name, loc, forks, stars, commits_count, developers]
        all_project_data.append(project_data)

    # Nome do arquivo CSV
    csv_file = 'projeto_dados.csv'

    # Escrever os dados no arquivo CSV
    with open(csv_file, 'w', newline='') as file:
        writer = csv.writer(file)

        # Escrever o cabeçalho
        header = ['Organização','Projeto', 'LOC', 'Forks', 'Stars', 'Commits', 'Developers']
        writer.writerow(header)

        # Escrever os dados de cada projeto
        writer.writerows(all_project_data)

    print(f'Dados salvos em {csv_file}')
