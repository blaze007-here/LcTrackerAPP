package com.example.lctracker.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.lctracker.R

class LcTrackerWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            val prefs = context.getSharedPreferences("lc_prefs", Context.MODE_PRIVATE)
            val solved = prefs.getInt("solved", 0)
            val total = prefs.getInt("total", 0)

            val views = RemoteViews(context.packageName, R.layout.leetcodetracker_widget).apply {
                setTextViewText(R.id.widget_stats, "Solved: $solved / $total")
            }

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}