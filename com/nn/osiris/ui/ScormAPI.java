package com.nn.osiris.ui;

public interface ScormAPI {
    
    public String initialize();
    public String terminate();
    public String getValue(String name);
    public String setValue(String name, String value);
    public String getLastError();
    public String getErrorString();
    public String getDiagnostic();
    public String commit();
    
}
