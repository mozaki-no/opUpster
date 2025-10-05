package app.opcsv.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

  private String baseUrl;
  private String apiToken;
  private int projectId;
  private String csvPath;
  private int external_keyCustomFieldId;
  private boolean dryRun;

  public static class Proxy {
    private boolean enabled;
    private String host;
    private int port;
    private String username;
    private String password;
    private String nonProxyHosts;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNonProxyHosts() { return nonProxyHosts; }
    public void setNonProxyHosts(String nonProxyHosts) { this.nonProxyHosts = nonProxyHosts; }
  }

  private Proxy proxy = new Proxy();

  public String getBaseUrl() { return baseUrl; }
  public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
  public String getApiToken() { return apiToken; }
  public void setApiToken(String apiToken) { this.apiToken = apiToken; }
  public int getProjectId() { return projectId; }
  public void setProjectId(int projectId) { this.projectId = projectId; }
  public String getCsvPath() { return csvPath; }
  public void setCsvPath(String csvPath) { this.csvPath = csvPath; }
  public int getexternal_keyCustomFieldId() { return external_keyCustomFieldId; }
  public void setexternal_keyCustomFieldId(int external_keyCustomFieldId) { this.external_keyCustomFieldId = external_keyCustomFieldId; }
  public boolean isDryRun() { return dryRun; }
  public void setDryRun(boolean dryRun) { this.dryRun = dryRun; }
  public Proxy getProxy() { return proxy; }
  public void setProxy(Proxy proxy) { this.proxy = proxy; }
}
