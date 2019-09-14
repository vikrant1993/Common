package vk.help.network

interface ResultsListener {
    fun onResultsSucceeded(result: NetworkResponse)
}