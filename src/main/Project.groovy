class Project {
    
    private String name
    private String path

    public Project(String name, String path) {
        this.name = name
        this.path = path
    }

    public String getName() {
        return name
    }

    public String getPath() {
        return path
    }

    public void setName(String name) {
        this.name = name
    }

    public void setPath(String path) {
        this.path = path
    }
}