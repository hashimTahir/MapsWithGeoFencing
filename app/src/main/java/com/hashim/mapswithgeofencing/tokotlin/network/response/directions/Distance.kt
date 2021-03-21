/*
 * Copyright (c) 2021/  3/ 22.  Created by Hashim Tahir
 */

package com.hashim.mapswithgeofencing.tokotlin.network.response.directions


import com.google.gson.annotations.SerializedName

data class Distance(
    @SerializedName("text")
    val text: String,
    @SerializedName("value")
    val value: Int
)