package com.acube.audii.repository.audioBook

import com.acube.audii.model.database.Datasource
import com.acube.audii.model.database.DatasourceDao
import javax.inject.Inject

interface DatasourceRepository{
    suspend fun getAllDatasources(): List<Datasource>
    suspend fun getDatasourceById(id: String): Datasource?
    suspend fun addDatasource(datasource: Datasource)
    suspend fun deleteDatasource(id: String)
}

class DatasourceRepositoryImpl @Inject constructor(private val dataSourceDao: DatasourceDao):DatasourceRepository{
    override suspend fun getAllDatasources(): List<Datasource> {
        return dataSourceDao.getAllDatasources()
    }

    override suspend fun getDatasourceById(id: String): Datasource? {
        return dataSourceDao.getDatasourceById(id)
    }

    override suspend fun addDatasource(datasource: Datasource) {
        return dataSourceDao.insertDatasource(datasource)
    }

    override suspend fun deleteDatasource(id: String) {
        return dataSourceDao.deleteDatasource(id)
    }

}