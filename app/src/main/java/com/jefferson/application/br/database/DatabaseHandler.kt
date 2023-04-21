/*
 * Copyright (C) 2023 Jefferson Calheiros


 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.jefferson.application.br.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.jefferson.application.br.Contact

class DatabaseHandler(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    // Creating Tables
    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_CONTACTS_TABLE = ("CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_PH_NO + " TEXT," + KEY_IMAGE + " BLOB);")
        db.execSQL(CREATE_CONTACTS_TABLE)
    }

    // Upgrading database
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS)

        // Create tables again
        onCreate(db)
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */
    // Adding new contact
    fun addContact(contact: Contact) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_NAME, contact.name) // Contact Name
        values.put(KEY_PH_NO, contact.phoneNumber) // Contact Phone
        values.put(KEY_IMAGE, contact.phonePhoto)
        // Inserting Row
        db.insert(TABLE_CONTACTS, null, values)
        db.close() // Closing database connection
    }

    // Getting single contact
    fun getContact(id: Int): Contact {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_CONTACTS,
            arrayOf(
                KEY_ID,
                KEY_NAME, KEY_PH_NO
            ),
            KEY_ID + "=?",
            arrayOf(id.toString()),
            null,
            null,
            null,
            null
        )
        cursor?.moveToFirst()
        // return contact
        return Contact(
            cursor!!.getString(0).toInt(),
            cursor.getString(1), cursor.getString(2)
        )
    }

    // Getting All Contacts
    fun CreateTable(table_name: String) {
        val db = writableDatabase
        val string_db = "CREATE TABLE $table_name(ID INTEGER PRIMARY KEY, PASS TEXT)"
        db.execSQL(string_db)
    }// Adding contact to list

    // return contact list
    // Select All Query
    val allContacts:

    // looping through all rows and adding to list
            List<Contact>
        get() {
            val contactList: MutableList<Contact> = ArrayList()
            // Select All Query
            val selectQuery = "SELECT  * FROM " + TABLE_CONTACTS
            val db = this.writableDatabase
            val cursor = db.rawQuery(selectQuery, null)

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    val contact = Contact()
                    contact.id = cursor.getString(0).toInt()
                    contact.name = cursor.getString(1)
                    contact.phoneNumber = cursor.getString(2)
                    contact.setPhoneImage(cursor.getBlob(3))
                    // Adding contact to list
                    contactList.add(contact)
                } while (cursor.moveToNext())
            }

            // return contact list
            return contactList
        }

    // Updating single contact
    fun updateContact(contact: Contact): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(KEY_NAME, contact.name)
        values.put(KEY_PH_NO, contact.phoneNumber)
        values.put(KEY_IMAGE, contact.phonePhoto)

        // updating row
        return db.update(TABLE_CONTACTS, values, KEY_ID + " = ?", arrayOf(contact.id.toString()))
    }

    // Deleting single contact
    fun deleteContact(contact: Contact) {
        val db = this.writableDatabase
        db.delete(TABLE_CONTACTS, KEY_ID + " = ?", arrayOf(contact.id.toString()))
        db.close()
    }// return count

    // Getting contacts Count
    val contactsCount: Int
        get() {
            val countQuery = "SELECT  * FROM " + TABLE_CONTACTS
            val db = this.readableDatabase
            val cursor = db.rawQuery(countQuery, null)
            cursor.close()

            // return count
            return cursor.count
        }

    companion object {
        // All Static variables
        private const val DATABASE_VERSION = 1

        // Database Name
        private const val DATABASE_NAME = "contactsManager"

        // Contacts table name
        private const val TABLE_CONTACTS = "contacts"

        // Contacts Table Columns names
        private const val KEY_ID = "id"
        private const val KEY_NAME = "name"
        private const val KEY_PH_NO = "phone_number"
        private const val KEY_IMAGE = "image_data"
    }
}