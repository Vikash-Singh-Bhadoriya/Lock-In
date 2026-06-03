package com.vikashsinghapp.lockin.system.alarm

object AlarmIdFactory {

    // --- STATIC IDS ---
    const val DAILY_REMINDER_ID = 999
    const val NIGHTLY_REPORT_ID = 888

    // --- ALARM REQUEST CODES ---
    fun getMainAlarmId(taskId: Long): Int = taskId.toInt()
    fun getPrepNudgeAlarmId(taskId: Long): Int = taskId.toInt() + 50000
    fun getDeadMansSwitchId(taskId: Long): Int = taskId.toInt() + 80000

    // --- NOTIFICATION IDS ---
    fun getPrepNudgeNotificationId(taskId: Long): Int = taskId.toInt() + 5000
    fun getRollCallNotificationId(taskId: Long): Int = taskId.toInt() + 40000
    fun getMissedSessionNotificationId(taskId: Long): Int = taskId.toInt() + 20000

    // --- NOTIFICATION ACTION REQUEST CODES ---
    fun getPrepReadyActionId(taskId: Long): Int = taskId.toInt() + 60000
    fun getPrepDelayActionId(taskId: Long): Int = taskId.toInt() + 10000
    fun getMissedSessionTapId(taskId: Long): Int = taskId.toInt() + 30000
}