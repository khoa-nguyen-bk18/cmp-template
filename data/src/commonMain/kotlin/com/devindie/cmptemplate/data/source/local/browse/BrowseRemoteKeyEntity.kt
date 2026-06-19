package com.devindie.cmptemplate.data.source.local.browse

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class BrowseRemoteKeyEntity(@PrimaryKey val key: String, val nextPage: Int?)
