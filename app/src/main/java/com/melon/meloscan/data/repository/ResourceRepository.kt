package com.melon.meloscan.data.repository

import com.melon.meloscan.data.supabase.SupabaseClientProvider
import com.melon.meloscan.model.Resource
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class ResourceRepository {

    private val supabase =
        SupabaseClientProvider.client

    suspend fun getActiveResources(): List<Resource> {

        return supabase
            .from("resources")
            .select {

                filter {
                    eq(
                        "is_active",
                        true
                    )
                }

                order(
                    column = "created_at",
                    order = Order.DESCENDING
                )
            }
            .decodeList<Resource>()
    }
}