package edu.uchicago.gerber.favs.presentation.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import edu.uchicago.gerber.favs.common.Constants
import edu.uchicago.gerber.favs.data.models.Item
import edu.uchicago.gerber.favs.data.repository.ApiProvider
import edu.uchicago.gerber.favs.data.repository.BooksRepository
import edu.uchicago.gerber.favs.presentation.screens.search.paging.BookSource
import edu.uchicago.gerber.favs.presentation.screens.search.paging.Paginate
import edu.uchicago.gerber.favs.presentation.screens.search.paging.SearchOperation
import edu.uchicago.gerber.favs.presentation.screens.search.paging.SearchState
import kotlinx.coroutines.launch

class BookViewModel : ViewModel() {

    private val booksRepository: BooksRepository = BooksRepository(ApiProvider.booksApi())

    //////////////////////////////////////////
    // MUTABLE-STATES AND OBSERVABLE STATES
    //////////////////////////////////////////
    private var _queryText = mutableStateOf("")
    val queryText: State<String> = _queryText


    private var _book = mutableStateOf(Constants.fakeBook)
    val book: State<Item> = _book

    private val _searchState = mutableStateOf(SearchState())
    val searchState: State<SearchState> = _searchState

    //////////////////////////////////////////
    // FUNCTIONS
    //////////////////////////////////////////
    fun setBook(book: Item) {
        _book.value = book
    }

    fun setQueryText(query: String) {
        _queryText.value = query
    }


    fun onSearch() {
        _searchState.value = SearchState(searchOperation = SearchOperation.LOADING)
        viewModelScope.launch {
            _searchState.value = SearchState(
                data = Pager(
                    config = PagingConfig(pageSize = 10, prefetchDistance = 5),
                    pagingSourceFactory = {
                        BookSource(
                            booksRepository = booksRepository,
                            paginateData = Paginate(
                                query = _queryText.value,
                            )
                        )
                    }
                ).flow.cachedIn(viewModelScope),
                searchOperation = SearchOperation.DONE
            )
        }
    }


}