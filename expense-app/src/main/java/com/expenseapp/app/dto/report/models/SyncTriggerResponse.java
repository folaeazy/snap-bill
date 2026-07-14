package com.expenseapp.app.dto.report.models;

public  record SyncTriggerResponse(int triggered, int alreadySyncing, String message) {}
