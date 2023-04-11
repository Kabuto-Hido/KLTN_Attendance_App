package hcmute.edu.vn.tlcn.attendanceapp.pattern;

import hcmute.edu.vn.tlcn.attendanceapp.model.Config;

public class Config_singleton {
    private Config config = null;
    static Config_singleton config_singleton;

    public Config_singleton() {
    }

    public static Config_singleton getInstance(){
        if (config_singleton == null) {
            config_singleton = new Config_singleton();
        }
        return config_singleton;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }
}
