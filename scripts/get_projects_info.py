import requests
import re
import csv


# Lista para armazenar os dados de todos os projetos
all_project_data = []

# Defina seu token de acesso pessoal do GitHub aqui
ACCESS_TOKEN = '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'


def commitCount(u, r):
    headers = {'Authorization': 'token ' + ACCESS_TOKEN}
    return re.search('\d+$', requests.get('https://api.github.com/repos/{}/{}/commits?per_page=1'.format(u, r), headers=headers).links['last']['url']).group()

def commitDevs(u, r):
    headers = {'Authorization': 'token ' + ACCESS_TOKEN}
    return re.search('\d+$', requests.get('https://api.github.com/repos/{}/{}/contributors?per_page=1&anon=true'.format(u, r), headers=headers).links['last']['url']).group()


def get_repository_data(owner, repo):
    base_url = 'https://api.github.com/repos/{}/{}'.format(owner, repo)
    headers = {'Authorization': 'token ' + ACCESS_TOKEN}

    response = requests.get(base_url, headers=headers)
    data = response.json()
    
    organization = data['owner']['login']
    project_name = data['name']
    loc = data['size']
    forks = data['forks_count']
    stars = data['stargazers_count']

    #contributors_url = base_url + '/contributors'
    #contributors_response = requests.get(contributors_url, headers=headers)
    #contributors_data = contributors_response.json()
    developers = commitDevs(owner, repo)

    #commits_url = base_url + '/commits?per_page=1'
    #commits_response = requests.get(commits_url, headers=headers)
    #commits_data = commits_response.json()

    commits_count = commitCount(owner, repo)

    return organization, project_name, loc, forks, stars, commits_count, developers

if __name__ == '__main__':
    projects = [['brettwooldridge', 'HikariCP'], ['PhilJay', 'MPAndroidChart'], ['opentripplanner', 'OpenTripPlanner'], ['ReactiveX', 'RxJava'], ['Netflix', 'SimianArmy'], ['Activiti', 'activiti'], ['Alluxio', 'alluxio'], ['antlr', 'antlr4'], ['CloudSlang', 'cloud-slang'], ['yasserg', 'crawler4j'], ['cucumber', 'cucumber-jvm'], ['dropwizard', 'dropwizard'], ['apache', 'druid'], ['elastic', 'elasticsearch'], ['richardwilly98', 'elasticsearch-river-mongodb'], ['unclebob', 'fitnesse'], ['hector-client', 'hector'], ['DiUS', 'java-faker'], ['jenkinsci', 'jenkins'], ['jhy', 'jsoup'], ['libgdx', 'libgdx'], ['qos-ch', 'logback'], ['netty', 'netty'], ['square', 'okhttp'], ['orientechnologies', 'orientdb'], ['jchambers', 'pushy'], ['sanity', 'quickml'], ['resty-gwt', 'resty-gwt'], ['square', 'retrofit'], ['spring-projects', 'spring-boot'], ['apache', 'storm'], ['swagger-api', 'swagger-core'], ['kongchen', 'swagger-maven-plugin'], ['thinkaurelius', 'titan'], ['vavr-io', 'vavr'], ['voldemort', 'voldemort'], ['webbit', 'webbit']]
    for project in projects:
        organization, project_name, loc, forks, stars, commits_count, developers = get_repository_data(project[0],project[1])
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

