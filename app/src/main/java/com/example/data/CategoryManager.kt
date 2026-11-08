package com.example.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class CategoryItem(
    val name: String,
    val icon: String = "🏷️",
    val colorHex: String = "#3B82F6",
    val notifyDaysBefore: Int = 1,
    val customReminders: List<Int> = if (notifyDaysBefore > 0) listOf(notifyDaysBefore) else emptyList()
)

object CategoryManager {

    private const val PREFS_NAME = "evenza_category_prefs"
    private const val KEY_CATEGORIES = "custom_categories_json"

    val DEFAULT_CATEGORIES = listOf(
        CategoryItem(name = "Food", icon = "🍞", colorHex = "#F97316", notifyDaysBefore = 1, customReminders = listOf(1)),
        CategoryItem(name = "Medicine", icon = "💊", colorHex = "#EC4899", notifyDaysBefore = 3, customReminders = listOf(1, 3)),
        CategoryItem(name = "Dairy", icon = "🥛", colorHex = "#3B82F6", notifyDaysBefore = 1, customReminders = listOf(1)),
        CategoryItem(name = "Beverage", icon = "🧃", colorHex = "#10B981", notifyDaysBefore = 1, customReminders = listOf(1)),
        CategoryItem(name = "Pantry", icon = "🥫", colorHex = "#EAB308", notifyDaysBefore = 7, customReminders = listOf(2, 7)),
        CategoryItem(name = "Cosmetics", icon = "💄", colorHex = "#8B5CF6", notifyDaysBefore = 14, customReminders = listOf(7, 14)),
        CategoryItem(name = "General", icon = "⏳", colorHex = "#6366F1", notifyDaysBefore = 1, customReminders = listOf(1))
    )

    fun getCategories(context: Context): List<CategoryItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_CATEGORIES, null)
        if (jsonStr.isNullOrBlank()) {
            return DEFAULT_CATEGORIES
        }

        return try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<CategoryItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val rawReminders = obj.optJSONArray("customReminders")
                val remindersList = mutableListOf<Int>()
                if (rawReminders != null) {
                    for (r in 0 until rawReminders.length()) {
                        val d = rawReminders.getInt(r)
                        if (d > 0 && !remindersList.contains(d)) remindersList.add(d)
                    }
                }
                val notifyDays = obj.optInt("notifyDaysBefore", 1)
                if (remindersList.isEmpty() && notifyDays > 0) {
                    remindersList.add(notifyDays)
                }
                remindersList.sort()
                list.add(
                    CategoryItem(
                        name = obj.getString("name"),
                        icon = obj.optString("icon", "🏷️"),
                        colorHex = obj.optString("colorHex", "#3B82F6"),
                        notifyDaysBefore = remindersList.firstOrNull() ?: notifyDays,
                        customReminders = remindersList
                    )
                )
            }
            if (list.isEmpty()) DEFAULT_CATEGORIES else list
        } catch (_: Exception) {
            DEFAULT_CATEGORIES
        }
    }

    fun saveCategories(context: Context, categories: List<CategoryItem>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        for (item in categories) {
            val sortedReminders = item.customReminders.filter { it > 0 }.distinct().sorted()
            val remindersArray = JSONArray()
            sortedReminders.forEach { remindersArray.put(it) }

            val obj = JSONObject().apply {
                put("name", item.name)
                put("icon", item.icon)
                put("colorHex", item.colorHex)
                put("notifyDaysBefore", sortedReminders.firstOrNull() ?: item.notifyDaysBefore)
                put("customReminders", remindersArray)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_CATEGORIES, jsonArray.toString()).apply()
    }

    fun addCategory(context: Context, item: CategoryItem) {
        val current = getCategories(context).toMutableList()
        if (current.none { it.name.equals(item.name.trim(), ignoreCase = true) }) {
            current.add(item.copy(name = item.name.trim()))
            saveCategories(context, current)
        }
    }

    fun updateCategory(context: Context, oldName: String, updatedItem: CategoryItem) {
        val current = getCategories(context).toMutableList()
        val index = current.indexOfFirst { it.name.equals(oldName.trim(), ignoreCase = true) }
        if (index != -1) {
            current[index] = updatedItem.copy(name = updatedItem.name.trim())
            saveCategories(context, current)
        }
    }

    fun deleteCategory(context: Context, name: String) {
        val current = getCategories(context).toMutableList()
        current.removeAll { it.name.equals(name.trim(), ignoreCase = true) }
        if (current.isEmpty()) {
            saveCategories(context, DEFAULT_CATEGORIES)
        } else {
            saveCategories(context, current)
        }
    }

    fun getCategoryByName(context: Context, name: String): CategoryItem {
        val list = getCategories(context)
        return list.firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }
            ?: list.firstOrNull { it.name.equals("General", ignoreCase = true) }
            ?: CategoryItem(name = name, icon = "⏳", colorHex = "#6366F1", notifyDaysBefore = 1)
    }
}
