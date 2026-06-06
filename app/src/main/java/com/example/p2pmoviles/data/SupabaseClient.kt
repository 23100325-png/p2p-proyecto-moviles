package com.example.p2pmoviles.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage // 🟢 Verifica este import

object SupabaseClient {
    // Reemplaza esto con tu URL real de Supabase
    private const val SUPABASE_URL = "https://kspajhtuztqzcnrdazho.supabase.co"

    // Reemplaza esto con tu clave anónima real de Supabase
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImtzcGFqaHR1enRxemNucmRhemhvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODAxODI2MjUsImV4cCI6MjA5NTc1ODYyNX0.jqfNc20AcGpDfoYJC5rBwLIXZftmd5NpNAfZAZAEYEA"

    // Este es el cliente único que usará toda la app
    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        // Instalamos los módulos/funciones que vamos a utilizar
        install(Postgrest) // Activa la capacidad de hacer consultas SQL (Select, Insert, Update)
        install(Auth)      // Activa el control de usuarios (signIn, signUp, currentUser)
        install(Storage) // 🟢 ESTO DEBE ESTAR AQUÍ INSTALADO
    }
}