package com.ybh.dfs.namenaode.server;

public class Command {
    public static final Integer REGISTER = 1;
    public static final Integer REPORT_COMPLETE_DATANODES = 2;
    public static final Integer REPLICATE = 3;
    public static final Integer REMOVE_REPLICATE = 4;

    public Command() {
    }

    public Command(Integer type) {
        this.type = type;
    }

    private Integer type;
    private String content;

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Command{" +
                "type=" + type +
                ", content='" + content + '\'' +
                '}';
    }
}
