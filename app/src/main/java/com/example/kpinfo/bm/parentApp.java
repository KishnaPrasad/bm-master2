package com.example.kpinfo.bm;

public class parentApp {
    private String pkgName;
    private String lastTimeUsed;
    private String totalTimeInForeground;
    private String childStats;

    public parentApp(){}



    public parentApp(String pkgName, String lastTimeUsed, String totalTimeInForeground) {
        this.pkgName = pkgName;
        this.lastTimeUsed = lastTimeUsed;
        this.totalTimeInForeground = totalTimeInForeground;

    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String getLastTimeUsed() {
        return lastTimeUsed;
    }

    public void setLastTimeUsed(String lastTimeUsed) {
        this.lastTimeUsed = lastTimeUsed;
    }

    public String getTotalTimeInForeground() {
        return totalTimeInForeground;
    }

    public void setTotalTimeInForeground(String totalTimeInForeground) {
        this.totalTimeInForeground = totalTimeInForeground;
    }
    public String getChildStats() {
        return childStats;
    }

    public void setChildStats(String childStats) {
        this.childStats = childStats;
    }
}
