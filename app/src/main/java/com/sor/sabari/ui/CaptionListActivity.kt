package com.sor.sabari.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

import androidx.compose.ui.ExperimentalComposeUiApi
import com.sor.sabari.ui.theme.AppTheme

class CaptionListActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("test1","activity created")
        setContent {
            AppTheme{
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    CaptionCacheApp(CaptionListViewModel())
                }
            }
        }
    }




}
