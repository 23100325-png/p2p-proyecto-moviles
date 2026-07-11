import java.util.Properties // 🟢 Mantenemos el import arriba del todo

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.p2pmoviles"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.p2pmoviles"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // =========================================================================
        // 🔐 BLOQUE UNIFICADO Y SEGURO PARA LOCAL.PROPERTIES (Evita errores de scope)
        // =========================================================================
        val localPropertiesFile = rootProject.file("local.properties")
        val properties = Properties()

        var exchangeApiKey = ""
        var supabaseUrl = ""
        var supabaseKey = ""

        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { stream ->
                properties.load(stream)
            }
            // Cargamos los valores limpiando comillas si existieran
            supabaseUrl = properties.getProperty("SUPABASE_URL")?.replace("\"", "") ?: ""
            supabaseKey = properties.getProperty("SUPABASE_KEY")?.replace("\"", "") ?: ""
        }

        // Inyectamos las 3 variables de forma segura y limpia como Strings válidos
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_KEY", "\"$supabaseKey\"")
        // =========================================================================
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Supabase Componentes
    val supabaseVersion = "2.5.2"
    implementation("io.github.jan-tennert.supabase:postgrest-kt:$supabaseVersion")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:$supabaseVersion")
    implementation("io.github.jan-tennert.supabase:storage-kt:2.5.2")
    implementation("io.github.jan-tennert.supabase:realtime-kt:$supabaseVersion")

    // Motor HTTP (Ktor)
    implementation("io.ktor:ktor-client-android:2.3.11")

    // Serialización y UI
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.coil.compose)
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Retrofit para la API del Tipo de Cambio Real
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
}