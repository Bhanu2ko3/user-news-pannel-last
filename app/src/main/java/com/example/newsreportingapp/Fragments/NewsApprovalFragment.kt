// NewsApprovalFragment.kt
package com.example.newsreportingapp.Fragments

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsreportingapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.newsreportingapp.NewsApprovalAdapter
import com.example.newsreportingapp.Fragments.Post

class NewsApprovalFragment : Fragment(R.layout.approval) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var newsApprovalAdapter: NewsApprovalAdapter
    private lateinit var newsList: MutableList<Post>
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        recyclerView = view.findViewById(R.id.newsApprovalRecyclerView)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        newsList = mutableListOf()
        newsApprovalAdapter = NewsApprovalAdapter(newsList)
        recyclerView.adapter = newsApprovalAdapter

        swipeRefreshLayout.setOnRefreshListener {
            fetchUserNews() // Fetch only the logged-in user's news
        }

        fetchUserNews()

        val searchInput = view.findViewById<EditText>(R.id.searchInput)
        val searchButton = view.findViewById<ImageButton>(R.id.searchButton)
        searchButton.setOnClickListener {
            val query = searchInput.text.toString().trim()
            if (query.isNotEmpty()) {
                searchUserNews(query) // Search only the user's news
            } else {
                fetchUserNews()
            }
        }
    }

    private fun fetchUserNews() {
        swipeRefreshLayout.isRefreshing = true
        val currentUser = auth.currentUser
        if (currentUser == null) {
            swipeRefreshLayout.isRefreshing = false
            return
        }

        val userNewsList = mutableListOf<Post>()

        val approvedTask = db.collection("newsApproved")
            .whereEqualTo("reporter", currentUser.displayName) // Filter by reporter username
            .get()

        val rejectedTask = db.collection("newsReject")
            .whereEqualTo("reporter", currentUser.displayName) // Filter rejected news
            .get()

        approvedTask.addOnSuccessListener { approvedResult ->
            val approvedList = approvedResult.documents.mapNotNull { it.toObject(Post::class.java)?.apply { status = "approved" } }
            userNewsList.addAll(approvedList)

            rejectedTask.addOnSuccessListener { rejectedResult ->
                val rejectedList = rejectedResult.documents.mapNotNull { it.toObject(Post::class.java)?.apply { status = "rejected" } }
                userNewsList.addAll(rejectedList)

                userNewsList.sortByDescending { it.createdAt?.seconds ?: 0L }
                newsApprovalAdapter.updateNewsList(userNewsList)
                swipeRefreshLayout.isRefreshing = false
            }.addOnFailureListener {
                swipeRefreshLayout.isRefreshing = false
            }
        }.addOnFailureListener {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun searchUserNews(query: String) {
        swipeRefreshLayout.isRefreshing = true
        val currentUser = auth.currentUser
        if (currentUser == null) {
            swipeRefreshLayout.isRefreshing = false
            return
        }

        val userNewsList = mutableListOf<Post>()

        val approvedTask = db.collection("newsApproved")
            .whereEqualTo("reporter", currentUser.displayName)
            .orderBy("topic") // Ensure Firestore index is created for this field
            .startAt(query)
            .endAt(query + "\uf8ff")
            .get()

        val rejectedTask = db.collection("newsReject")
            .whereEqualTo("reporter", currentUser.displayName)
            .orderBy("topic")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .get()

        approvedTask.addOnSuccessListener { approvedResult ->
            val approvedList = approvedResult.documents.mapNotNull { it.toObject(Post::class.java)?.apply { status = "approved" } }
            userNewsList.addAll(approvedList)

            rejectedTask.addOnSuccessListener { rejectedResult ->
                val rejectedList = rejectedResult.documents.mapNotNull { it.toObject(Post::class.java)?.apply { status = "rejected" } }
                userNewsList.addAll(rejectedList)

                userNewsList.sortByDescending { it.createdAt?.seconds ?: 0L }
                newsApprovalAdapter.updateNewsList(userNewsList)
                swipeRefreshLayout.isRefreshing = false
            }.addOnFailureListener {
                swipeRefreshLayout.isRefreshing = false
            }
        }.addOnFailureListener {
            swipeRefreshLayout.isRefreshing = false
        }
    }
}
