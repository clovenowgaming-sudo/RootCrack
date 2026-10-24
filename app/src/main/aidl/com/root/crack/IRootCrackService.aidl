package com.root.crack;

interface IRootCrackService {
    String runDaemon(in String[] args);
    boolean isDaemonAlive();
}
