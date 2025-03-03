import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsreportingapp.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.newsreportingapp.Fragments.Post

class HomeFragment : Fragment(R.layout.home_activity) {
    private lateinit var postGrid: RecyclerView
    private lateinit var newsAdapter: NewsAdapter
    private val newsList = mutableListOf<Post>()
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var searchInput: EditText
    private lateinit var searchButton: ImageButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postGrid = view.findViewById(R.id.postGrid)
        postGrid.layoutManager = LinearLayoutManager(requireContext())
        newsAdapter = NewsAdapter(newsList)
        postGrid.adapter = newsAdapter

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        searchInput = view.findViewById(R.id.searchInput)
        searchButton = view.findViewById(R.id.searchButton)

        // Set up SwipeRefreshLayout listener
        swipeRefreshLayout.setOnRefreshListener {
            fetchNews()
        }

        // Fetch news initially
        fetchNews()

        // Set up Search button listener
        searchButton.setOnClickListener {
            val query = searchInput.text.toString().trim()
            if (query.isNotEmpty()) {
                searchNewsByReporter(query)
            } else {
                fetchNews()
            }
        }
    }

    private fun fetchNews() {
        swipeRefreshLayout.isRefreshing = true

        CoroutineScope(Dispatchers.IO).launch {
            try {
                firestore.collection("newsApproved")
                    .get()
                    .addOnSuccessListener { result ->
                        val posts = result.documents.mapNotNull { it.toObject(Post::class.java) }
                        activity?.runOnUiThread {
                            newsList.clear()
                            newsList.addAll(posts)
                            newsAdapter.notifyDataSetChanged()
                            swipeRefreshLayout.isRefreshing = false
                        }
                    }
                    .addOnFailureListener { e ->
                        activity?.runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "Failed to load news: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            swipeRefreshLayout.isRefreshing = false
                        }
                    }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun searchNewsByReporter(reporterName: String) {
        swipeRefreshLayout.isRefreshing = true

        val queryStart = reporterName.lowercase()
        val queryEnd = queryStart + "\uf8ff" // Unicode character to get all matching strings

        CoroutineScope(Dispatchers.IO).launch {
            try {
                firestore.collection("newsApproved")
                    .whereGreaterThanOrEqualTo("reporter", queryStart)
                    .whereLessThanOrEqualTo("reporter", queryEnd)
                    .get()
                    .addOnSuccessListener { result ->
                        val posts = result.documents.mapNotNull { it.toObject(Post::class.java) }
                        activity?.runOnUiThread {
                            newsList.clear()
                            newsList.addAll(posts)
                            newsAdapter.notifyDataSetChanged()
                            swipeRefreshLayout.isRefreshing = false
                        }
                    }
                    .addOnFailureListener { e ->
                        activity?.runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "Search failed: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            swipeRefreshLayout.isRefreshing = false
                        }
                    }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

}
