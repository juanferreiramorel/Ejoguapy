import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.myapplication"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        //URL y token de la API "people" para consultar RUC
        //se leen de local.properties (archivo local que NO se sube al repositorio):
        //  people.api.url=http://10.0.2.2:3000
        //  people.api.token=
        //10.0.2.2 = la PC anfitriona vista desde el emulador de Android
        //dispositivo fisico: usar la IP de la PC en la red local (ej: http://192.168.0.10:3000)
        //produccion: usar una URL https y cargar el token JWT en people.api.token
        val propiedadesLocales = Properties()
        val archivoLocal = rootProject.file("local.properties")
        if (archivoLocal.exists()) {
            archivoLocal.inputStream().use { propiedadesLocales.load(it) }
        }
        val peopleApiUrl = propiedadesLocales.getProperty("people.api.url", "").trim()
            .ifBlank { "http://10.0.2.2:3000" }
        val peopleApiToken = propiedadesLocales.getProperty("people.api.token", "").trim()
        //se escapan barras y comillas para generar un literal String valido en BuildConfig
        fun comoLiteral(valor: String) = "\"" + valor.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
        buildConfigField("String", "PEOPLE_API_URL", comoLiteral(peopleApiUrl))
        buildConfigField("String", "PEOPLE_API_TOKEN", comoLiteral(peopleApiToken))
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.glance:glance-appwidget:1.2.0")
    implementation("androidx.glance:glance-material3:1.2.0")
}