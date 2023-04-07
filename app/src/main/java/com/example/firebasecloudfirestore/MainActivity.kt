package com.example.firebasecloudfirestore

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.firebasecloudfirestore.databinding.ActivityMainBinding
import com.example.firebasecloudfirestore.models.Person
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val personColletionF = Firebase.firestore.collection("persons")
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.apply {
            btnAdd.setOnClickListener {
                val person = getOldPerson()
                savePerson(person)
            }
            btnRetrieve.setOnClickListener {
                retrievePerson()
            }
            btnUpdate.setOnClickListener {
                val oldPerson = getOldPerson()
                val newPersonMap = getNewPersonMap()
                updatePerson(oldPerson, newPersonMap)
            }
            btnDelete.setOnClickListener {
                val oldPerson = getOldPerson()
                deletePerson(oldPerson)
            }
        }
    }

    private fun retrievePerson() = CoroutineScope(Dispatchers.IO).launch {
        try {
            val querySnapshot = personColletionF.get().await()
            val sb = StringBuilder()
            for (document in querySnapshot.documents) {
                val person = document.toObject<Person>()
                sb.append("$person\n")
            }
            withContext(Dispatchers.Main) {
                binding.tvText.text = sb.toString()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun savePerson(person: Person) = CoroutineScope(Dispatchers.IO).launch {
        try {
            personColletionF.add(person).await()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Succesful saved data", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun getOldPerson(): Person {
        val firstname = binding.edtName.text.toString()
        val secondName = binding.edtSurname.text.toString()
        val age = binding.edtAge.text.toString().toInt()
        return Person(firstname, secondName, age)
    }

    private fun getNewPersonMap(): Map<String, Any> {
        val firstName = binding.newName.text.toString()
        val lastName = binding.newSurname.text.toString()
        val age = binding.newAge.text.toString()
        val map = mutableMapOf<String, Any>()
        if (firstName.isNotEmpty()) {
            map["firstName"] = firstName
        }
        if (lastName.isNotEmpty()) {
            map["lastName"] = lastName
        }
        if (age.isNotEmpty()) {
            map["age"] = age
        }
        return map
    }

    private fun updatePerson(person: Person, newPersonMap: Map<String, Any>) =
        CoroutineScope(Dispatchers.IO).launch {
            val personQuery = personColletionF
                .whereEqualTo("firstName", person.firstName)
                .whereEqualTo("lastName", person.lastName)
                .whereEqualTo("age", person.age)
                .get()
                .await()
            if (personQuery.documents.isNotEmpty()) {
                for (document in personQuery) {
                    try {
                        personColletionF.document(document.id).update("firstName", person.firstName)
                        personColletionF.document(document.id).set(
                            newPersonMap,
                            SetOptions.merge()
                        ).await()
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity,
                        "Bunday foydalanuvchi topilmadi",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun deletePerson(person: Person) = CoroutineScope(Dispatchers.IO).launch {
        val personQuery = personColletionF
            .whereEqualTo("firstName", person.firstName)
            .whereEqualTo("lastName", person.lastName)
            .whereEqualTo("age", person.age)
            .get()
            .await()
        if (personQuery.documents.isNotEmpty()) {
            for (document in personQuery) {
                try {
                    personColletionF.document(document.id).delete().await()
                    //Delete ism ni yoki age ni bittadan o'chirish
                    
                    /* personColletionF.document(document.id).update(mapOf(
                         "firstName" to FieldValue.delete()
                     ))*/
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity,
                    "Bunday foydalanuvchi topilmadi",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }
}