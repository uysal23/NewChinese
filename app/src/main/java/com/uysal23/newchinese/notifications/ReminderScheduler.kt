package com.uysal23.newchinese.notifications

import android.app.*
import android.content.*
import android.os.Build
import androidx.core.app.NotificationCompat
import com.uysal23.newchinese.MainActivity
import java.util.Calendar

data class ReminderSpec(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val days: Set<Int>,
    val message: String,
    val enabled: Boolean = true
) {
    fun encode(): String =
        listOf(
            id.toString(),
            hour.toString(),
            minute.toString(),
            days.sorted().joinToString(","),
            message.replace("|", " "),
            enabled.toString()
        ).joinToString("|")

    companion object {
        fun decode(raw: String): ReminderSpec? = runCatching {
            val p = raw.split("|")
            ReminderSpec(
                id = p[0].toInt(),
                hour = p[1].toInt(),
                minute = p[2].toInt(),
                days = p[3].split(",").filter { it.isNotBlank() }.map { it.toInt() }.toSet(),
                message = p[4],
                enabled = p.getOrNull(5)?.toBooleanStrictOrNull() ?: true
            )
        }.getOrNull()
    }
}

object ReminderScheduler {
    const val CHANNEL_ID = "study_reminders"

    fun schedule(context: Context, reminder: ReminderSpec) {
        cancel(context, reminder.id)
        if (!reminder.enabled) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        reminder.days.forEach { calendarDay ->
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("message", reminder.message)
                putExtra("reminderId", reminder.id)
            }
            val requestCode = reminder.id * 10 + calendarDay
            val pending = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val next = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, calendarDay)
                set(Calendar.HOUR_OF_DAY, reminder.hour)
                set(Calendar.MINUTE, reminder.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) add(Calendar.WEEK_OF_YEAR, 1)
            }

            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                next.timeInMillis,
                AlarmManager.INTERVAL_DAY * 7,
                pending
            )
        }
    }

    fun cancel(context: Context, reminderId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        (Calendar.SUNDAY..Calendar.SATURDAY).forEach { day ->
            val pending = PendingIntent.getBroadcast(
                context,
                reminderId * 10 + day,
                Intent(context, ReminderReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pending != null) alarmManager.cancel(pending)
        }
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    ReminderScheduler.CHANNEL_ID,
                    "Çince çalışma hatırlatıcıları",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }

        val launch = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val message = intent.getStringExtra("message")
            ?.takeIf { it.isNotBlank() }
            ?: "Bugünkü Çince çalışmanı unutma."

        val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("NewChinese")
            .setContentText(message)
            .setContentIntent(launch)
            .setAutoCancel(true)
            .build()

        manager.notify(intent.getIntExtra("reminderId", 1), notification)
    }
}
