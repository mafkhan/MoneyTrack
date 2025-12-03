package com.example.moneytrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moneytrack.data.TransactionDao
import com.example.moneytrack.data.TransactionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

class TransactionViewModel(private val transactionDao: TransactionDao) : ViewModel() {

    private val _allTransactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val allTransactions: StateFlow<List<TransactionEntity>> = _allTransactions.asStateFlow()

    private val _currentMonthTotal = MutableStateFlow(0.0)
    val currentMonthTotal: StateFlow<Double> = _currentMonthTotal.asStateFlow()

    private val _currentDayTotal = MutableStateFlow(0.0)
    val currentDayTotal: StateFlow<Double> = _currentDayTotal.asStateFlow()

    private val _lastTenTransactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val lastTenTransactions: StateFlow<List<TransactionEntity>> = _lastTenTransactions.asStateFlow()

    init {
        loadAllTransactions()
        loadLastTenTransactions()
        loadCurrentMonthTotal()
        loadCurrentDayTotal()
    }

    private fun loadAllTransactions() {
        viewModelScope.launch {
            transactionDao.getAllTransactions().collectLatest {
                _allTransactions.value = it
            }
        }
    }

    private fun loadLastTenTransactions() {
        viewModelScope.launch {
            transactionDao.getLastTenTransactions().collectLatest {
                _lastTenTransactions.value = it
            }
        }
    }

    fun loadCurrentMonthTotal() {
        viewModelScope.launch {
            val total = transactionDao.getCurrentMonthTotal()
            _currentMonthTotal.value = total ?: 0.0
        }
    }

    fun loadCurrentDayTotal() {
        viewModelScope.launch {
            val total = transactionDao.getCurrentDayTotal()
            _currentDayTotal.value = total ?: 0.0
        }
    }

    fun addTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionDao.insert(transaction)
        }
    }


    fun bulkUpdateExpenseTypes() {
        viewModelScope.launch {
            transactionDao.updateExpenseType("%carrefour%", "Grocery")
            transactionDao.updateExpenseType("%pizza hut%", "Food")
            transactionDao.updateExpenseType("%starbucks%", "Coffee")
            transactionDao.updateExpenseType("%Moon Light Automobile%","Car Maintenance")
            transactionDao.updateExpenseType("%Park Place Parking Car%","Car Maintenance")
            transactionDao.updateExpenseType("%Laam Technologies Inc%","Clothes")
            transactionDao.updateExpenseType("%Max%","Clothes")
            transactionDao.updateExpenseType("%Lril%","Clothes")
            transactionDao.updateExpenseType("%Brands For Less Llc Bf%","Clothes")
            transactionDao.updateExpenseType("%Adnoc Khalifa City 523%","Fuel")
            transactionDao.updateExpenseType("%Adnoc Embassy Area 782%","Fuel")
            transactionDao.updateExpenseType("%Adnoc Embassy Area 782%","Fuel")
            transactionDao.updateExpenseType("%Adnoc Embassy Area 782%","Fuel")
            transactionDao.updateExpenseType("%Adnoc Police College 7%","Fuel")
            transactionDao.updateExpenseType("%Adnoc Embassy Area 782%","Fuel")
            transactionDao.updateExpenseType("%Grand Emirates Market%","Gifts/Household")
            transactionDao.updateExpenseType("%Amazon.ae%","Gifts/Household")
            transactionDao.updateExpenseType("%Golden Green Baqala  L%","Grocery")
            transactionDao.updateExpenseType("%Carrefour Market Al Ra%","Grocery")
            transactionDao.updateExpenseType("%Carrefour Al Saqr%","Grocery")
            transactionDao.updateExpenseType("%Carrefour Al Saqr%","Grocery")
            transactionDao.updateExpenseType("%Carrefour Al Saqr%","Grocery")
            transactionDao.updateExpenseType("%Golden Green Baqala  L%","Grocery")
            transactionDao.updateExpenseType("%Noon Minutes%","Grocery")
            transactionDao.updateExpenseType("%Golden Green Baqala  L%","Grocery")
            transactionDao.updateExpenseType("%Noon Minutes%","Grocery")
            transactionDao.updateExpenseType("%Carrefour Market Al Ra%","Grocery")
            transactionDao.updateExpenseType("%Amazon Now%","Grocery")
            transactionDao.updateExpenseType("%Noon Minutes%","Grocery")
            transactionDao.updateExpenseType("%Amazon Grocery%","Grocery")
            transactionDao.updateExpenseType("%Carrefour Al Saqr%","Grocery")
            transactionDao.updateExpenseType("%Golden Green Baqala%","Grocery")
            transactionDao.updateExpenseType("%Noon Minutes%","Grocery")
            transactionDao.updateExpenseType("%Amazon Grocery%","Grocery")
            transactionDao.updateExpenseType("%Noon Minutes%","Grocery")
            transactionDao.updateExpenseType("%Noon Minutes%","Grocery")
            transactionDao.updateExpenseType("%Oasis Pure Water Facto%","Grocery")
            transactionDao.updateExpenseType("%West Zone Supermarket%","Grocery")
            transactionDao.updateExpenseType("%Noon Minutes%","Grocery")
            transactionDao.updateExpenseType("%Viva Danet Mall%","Grocery")
            transactionDao.updateExpenseType("%Pk Mart Fz Llc%","Grocery")
            transactionDao.updateExpenseType("%Pk Mart Fz Llc%","Grocery")
            transactionDao.updateExpenseType("%Pk Mart Fz Llc%","Grocery")
            transactionDao.updateExpenseType("%Noon Minutes%","Grocery")
            transactionDao.updateExpenseType("%Noon Minutes%","Grocery")
            transactionDao.updateExpenseType("%Amazonufg%","Grocery")
            transactionDao.updateExpenseType("%Noon Minutes%","Grocery")
            transactionDao.updateExpenseType("%Noon Minutes%","Grocery")
            transactionDao.updateExpenseType("%Careem Quik%","Grocery")
            transactionDao.updateExpenseType("%Noon Minutes%","Grocery")
            transactionDao.updateExpenseType("%Tamara%","Home Appliances")
            transactionDao.updateExpenseType("%Tamara%","Home Appliances")
            transactionDao.updateExpenseType("%Soft Touch Laundry%","Laundry")
            transactionDao.updateExpenseType("%Soft Touch Laundry%","Laundry")
            transactionDao.updateExpenseType("%Soft Touch Laundry%","Laundry")
            transactionDao.updateExpenseType("%Ziina* Afwancleaning%","Maid")
            transactionDao.updateExpenseType("%Mediclinic Hospitalsll%","Medical")
            transactionDao.updateExpenseType("%Dentacare Centre-br1%","Medical")
            transactionDao.updateExpenseType("%Mediclinic Hospitalsll%","Medical")
            transactionDao.updateExpenseType("%Mediclinic Hospitalsll%","Medical")
            transactionDao.updateExpenseType("%Accuro Specialist Supp%","Outside Food")
            transactionDao.updateExpenseType("%Tap*keeta%","Outside Food")
            transactionDao.updateExpenseType("%Islamabad Restaurant%","Outside Food")
            transactionDao.updateExpenseType("%Ifephy Br36auh17-1305l%","Outside Food")
            transactionDao.updateExpenseType("%Tap*keeta%","Outside Food")
            transactionDao.updateExpenseType("%Mann & Salwa Rest Llc%","Outside Food")
            transactionDao.updateExpenseType("%Cravia Inc Cinnabon%","Outside Food")
            transactionDao.updateExpenseType("%Bacolod Inasal Bbq Res%","Outside Food")
            transactionDao.updateExpenseType("%Jollibee%","Outside Food")
            transactionDao.updateExpenseType("%Manchow Wok Restaurant%","Outside Food")
            transactionDao.updateExpenseType("%Chowking Orient Restau%","Outside Food")
            transactionDao.updateExpenseType("%Carrefour Maf Mkt Raha%","Outside Food")
            transactionDao.updateExpenseType("%Accuro Specialist Supp%","Outside Food")
            transactionDao.updateExpenseType("%Al Fawar House Cafteri%","Outside Food")
            transactionDao.updateExpenseType("%Carrefour Market Al Ra%","Outside Food")
            transactionDao.updateExpenseType("%Mcdonalds-embassy Area%","Outside Food")
            transactionDao.updateExpenseType("%Zaika Palace Grill An%","Outside Food")
            transactionDao.updateExpenseType("%Afdal Restaurant And G%","Outside Food")
            transactionDao.updateExpenseType("%Carrefour Market Al Ra%","Outside Food")
            transactionDao.updateExpenseType("%Accuro Specialist Supp%","Outside Food")
            transactionDao.updateExpenseType("%Salt And Chillis Resta%","Outside Food")
            transactionDao.updateExpenseType("%Al Sahara Bakery%","Outside Food")
            transactionDao.updateExpenseType("%Talabat.com%","Outside Food")
            transactionDao.updateExpenseType("%Accuro Specialist Supp%","Outside Food")
            transactionDao.updateExpenseType("%Kfc%","Outside Food")
            transactionDao.updateExpenseType("%Circle K Mini Store Ll%","Outside Food")
            transactionDao.updateExpenseType("%Accuro Specialist Supp%","Outside Food")
            transactionDao.updateExpenseType("%Carrefour Market Al Ra%","Outside Food")
            transactionDao.updateExpenseType("%Carrefour Market Al Ra%","Outside Food")
            transactionDao.updateExpenseType("%Accuro Specialist Supp%","Outside Food")
            transactionDao.updateExpenseType("%Pizza Hut%","Outside Food")
            transactionDao.updateExpenseType("%Noon Food%","Outside Food")
            transactionDao.updateExpenseType("%Al Fawar House Cafteri%","Outside Food")
            transactionDao.updateExpenseType("%Waris Nihari%","Outside Food")
            transactionDao.updateExpenseType("%Tim Hortons%","Outside Food")
            transactionDao.updateExpenseType("%Royal Park Gents Saloo%","Self Care")
            transactionDao.updateExpenseType("%Arabia Taxi%","Taxi")
            transactionDao.updateExpenseType("%National Taxi%","Taxi")
            transactionDao.updateExpenseType("%Platinum Taxi Abu Dhab%","Taxi")
            transactionDao.updateExpenseType("%Platinum Taxi Abu Dhab%","Taxi")
            transactionDao.updateExpenseType("%Cars Taxi%","Taxi")
            transactionDao.updateExpenseType("%Arabia Taxi%","Taxi")
            transactionDao.updateExpenseType("%Aman Taxi%","Taxi")
            transactionDao.updateExpenseType("%Arabia Taxi%","Taxi")
            transactionDao.updateExpenseType("%National Taxi%","Taxi")
            transactionDao.updateExpenseType("%Aman Taxi%","Taxi")
            transactionDao.updateExpenseType("%Arabia Taxi%","Taxi")
            transactionDao.updateExpenseType("%Arabia Taxi%","Taxi")
            transactionDao.updateExpenseType("%Arabia Taxi%","Taxi")
            transactionDao.updateExpenseType("%Al Ghazal Transport%","Taxi")
            transactionDao.updateExpenseType("%Cars Taxi%","Taxi")
            transactionDao.updateExpenseType("%Ista Middle East Fze D%","Utilities")
            transactionDao.updateExpenseType("%Ista Middle East Fze D%","Utilities")
            transactionDao.updateExpenseType("%Q Mobility Llc%","Utilities")
            transactionDao.updateExpenseType("%Du No.-97145849354;%","Utilities")
            transactionDao.updateExpenseType("%Etisalat No.-054503776%","Utilities")
            transactionDao.updateExpenseType("%Etisalat No.-054431832%","Utilities")
            transactionDao.updateExpenseType("%Adwea No.-4280451597;%","Utilities")



        }
    }




}


@Suppress("UNCHECKED_CAST")
class TransactionViewModelFactory(private val transactionDao: TransactionDao) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            return TransactionViewModel(transactionDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

