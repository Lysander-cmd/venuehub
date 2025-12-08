package com.kelompok.venuehub.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClient {


    private const val SUPABASE_URL = "https://gwjheiqhigquieapcpaf.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imd3amhlaXFoaWdxdWllYXBjcGFmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQyODgwMTAsImV4cCI6MjA3OTg2NDAxMH0.bq6yRG-Q2lTXOC7Wu7UNO22RFWK7T-FIk8dReDZIyUk"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Postgrest)
        install(Auth)
        install(Storage)
    }
}