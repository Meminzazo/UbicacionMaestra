package com.esime.ubicacionmaestra.Firstapp.ui.homeActivity

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel: ViewModel() {


    private val _text = MutableLiveData<String>()
    val variableObservada: LiveData<String> = _text

   fun prueba(){
       viewModelScope.launch(Dispatchers.IO){
           _text.postValue("Craftersin")
       }
    }

}