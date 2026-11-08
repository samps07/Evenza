package com.example.widget

import android.content.Intent
import android.widget.RemoteViewsService

class CounterWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return CounterWidgetViewsFactory(applicationContext, intent)
    }
}
