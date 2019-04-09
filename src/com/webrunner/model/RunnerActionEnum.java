package com.webrunner.model;

/**
 * @author MarkHuang
 * @version <ul>
 * <li>2018/10/16, MarkHuang,new
 * </ul>
 * @since 2018/10/16
 */
public enum RunnerActionEnum {
    CLEAN("Clean project");
    public String action;
    RunnerActionEnum(String action){
        this.action  = action;
    }
}
