import android.content.Context
import android.content.SharedPreferences

class PreferencesHelper(context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    fun saveSetting(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    fun saveSetting(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
    }

    fun getSetting(key: String, defaultValue: String): String {
        return preferences.getString(key, defaultValue) ?: defaultValue
    }

    fun getSetting(key: String, defaultValue: Boolean): Boolean {
        return preferences.getBoolean(key, defaultValue) ?: defaultValue
    }
}
