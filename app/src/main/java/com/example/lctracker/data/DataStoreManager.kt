package com.example.lctracker.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.lctracker.ui.Difficulty
import com.example.lctracker.ui.Problem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

val Context.dataStore by preferencesDataStore(name = "problem_store")

class DataStoreManager(private val context: Context) {
    companion object {
        private val PROBLEM_LIST_KEY = stringPreferencesKey("problem_list")
    }

    suspend fun saveProblems(problems: List<Problem>) {
        val jsonArray = JSONArray()
        problems.forEach { problem ->
            val obj = JSONObject().apply {
                put("title", problem.title)
                put("isSolved", problem.isSolved)
                put("difficulty", problem.difficulty.name)
            }
            jsonArray.put(obj)
        }
        context.dataStore.edit { prefs ->
            prefs[PROBLEM_LIST_KEY] = jsonArray.toString()
        }
    }

    val problemsFlow: Flow<List<Problem>> = context.dataStore.data.map { prefs ->
        val jsonString = prefs[PROBLEM_LIST_KEY] ?: "[]"
        val jsonArray = JSONArray(jsonString)
        List(jsonArray.length()) { i ->
            val obj = jsonArray.getJSONObject(i)
            Problem(
                title = obj.getString("title"),
                isSolved = obj.getBoolean("isSolved"),
                difficulty = Difficulty.valueOf(obj.optString("difficulty", "EASY"))
            )
        }
    }
}
