package com.example.drawingrun

import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class AddRouteActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var touchOverlayView: TouchOverlayView

    private lateinit var drawButton: ImageButton
    private lateinit var deleteButton: ImageButton
    private lateinit var panButton: ImageButton  // 손 버튼 추가
    private lateinit var saveButton: ImageButton

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_route)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        touchOverlayView = findViewById(R.id.touchOverlay)

        // 🔸 ImageButton으로 타입 맞추기
        drawButton = findViewById(R.id.btnDraw)
        deleteButton = findViewById(R.id.btnDelete)
        panButton = findViewById(R.id.btnPan)  // 이동 버튼
        saveButton = findViewById(R.id.btnSave)

        touchOverlayView.mode = Mode.NONE
        updateButtonUI()

        drawButton.setOnClickListener {
            touchOverlayView.mode = if (touchOverlayView.mode != Mode.DRAW) Mode.DRAW else Mode.NONE
            updateButtonUI()
        }

        deleteButton.setOnClickListener {
            touchOverlayView.mode = if (touchOverlayView.mode != Mode.DELETE) Mode.DELETE else Mode.NONE
            updateButtonUI()
        }

        panButton.setOnClickListener {
            touchOverlayView.mode = Mode.NONE  // 이동 모드는 별도 동작 없고 터치 비활성화
            updateButtonUI()
        }

        saveButton.setOnClickListener {
            val allPoints = touchOverlayView.getAllPoints()
            if (allPoints.isEmpty()) {
                Toast.makeText(this, "저장할 경로가 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            showLabelSelectionBottomSheet { label, labelKr, iconUrl ->
                val geoPoints = allPoints.map { GeoPoint(it.latitude, it.longitude) }
                val userId = auth.currentUser?.uid ?: "unknown_user"

                val routeData = hashMapOf(
                    "points" to geoPoints,
                    "userId" to userId,
                    "label" to label,
                    "label_kr" to labelKr,
                    "iconUrl" to iconUrl,
                    "createdAt" to Timestamp.now()
                )

                firestore.collection("routes")
                    .add(routeData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "경로 저장 완료!", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "저장 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }


    override fun onMapReady(map: GoogleMap) {
        mMap = map
        touchOverlayView.map = mMap

        mMap.setOnCameraMoveListener {
            touchOverlayView.invalidate()
        }

        val seoul = LatLng(37.5665, 126.9780)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 15f))
    }

    private fun updateButtonUI() {
        val activeColor = Color.parseColor("#2196F3")
        val inactiveColor = Color.parseColor("#CCCCCC")

        drawButton.setColorFilter(if (touchOverlayView.mode == Mode.DRAW) activeColor else inactiveColor)
        deleteButton.setColorFilter(if (touchOverlayView.mode == Mode.DELETE) activeColor else inactiveColor)
        panButton.setColorFilter(if (touchOverlayView.mode == Mode.NONE) activeColor else inactiveColor)
    }

    private fun showLabelSelectionBottomSheet(
        onResult: (label: String, labelKr: String, iconUrl: String) -> Unit
    ) {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_label_selection, null)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(dialogView)

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recycler_label_icons)
        val editLabelKr = dialogView.findViewById<EditText>(R.id.edit_label_kr)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btn_confirm_label)

        recyclerView.layoutManager = GridLayoutManager(this, 4)

        firestore.collection("label_icon").get().addOnSuccessListener { snapshot ->
            val items = snapshot.mapNotNull { doc ->
                val id = doc.id.removePrefix("icon_")
                val url = doc.getString("iconURL") ?: return@mapNotNull null
                val labelKr = doc.getString("label_kr") ?: id  // 없으면 영어 아이디로 fallback
                LabelAdapter.LabelItem(id, labelKr, url)
            }

            var selectedItem: LabelAdapter.LabelItem? = null

            val adapter = LabelAdapter(
                items = items,
                onClick = { item -> selectedItem = item },
                onAddClick = {} // 사용하지 않음
            )
            recyclerView.adapter = adapter

            btnConfirm.setOnClickListener {
                val labelKr = editLabelKr.text.toString().trim()
                val selected = selectedItem
                if (labelKr.isNotEmpty() && selected != null) {
                    onResult(selected.label, labelKr, selected.iconUrl)
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "아이콘과 라벨 이름을 선택해주세요", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }
}
