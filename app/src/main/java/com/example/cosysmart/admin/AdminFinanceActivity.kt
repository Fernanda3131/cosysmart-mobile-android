package com.example.cosysmart.admin

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cosysmart.R
import com.example.cosysmart.utils.BottomNavigationHelper
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminFinanceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_finance)

        val btnBack = findViewById<ImageButton>(R.id.btnBackFinanzas)
        btnBack?.setOnClickListener {
            val intent = Intent(this, ActivityHomeAdmin::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        if (bottomNav != null) {
            BottomNavigationHelper.setupAdminNav(this, bottomNav, R.id.nav_finanzas)
        }
        setupBarChart()

        Toast.makeText(this, "Panel Financiero cargado con éxito", Toast.LENGTH_SHORT).show()
    }

    private fun setupBarChart() {
        val barChart = findViewById<BarChart>(R.id.barChartFinanzas)
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, 15.5f))
        entries.add(BarEntry(1f, 22.0f))
        entries.add(BarEntry(2f, 31.4f))
        entries.add(BarEntry(3f, 28.9f))
        entries.add(BarEntry(4f, 48.2f))

        val dataSet = BarDataSet(entries, "Ingresos Brutos (Millones COP)")
        dataSet.color = Color.parseColor("#C86D51")
        dataSet.valueTextColor = Color.parseColor("#3D3028")
        dataSet.valueTextSize = 11f

        val barData = BarData(dataSet)
        barData.barWidth = 0.5f
        barChart.data = barData

        val meses = arrayOf("Ene", "Feb", "Mar", "Abr", "May")
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(meses)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true
        xAxis.setDrawGridLines(false)
        xAxis.textColor = Color.parseColor("#7A6D63")
        xAxis.textSize = 12f

        barChart.description.isEnabled = false
        barChart.legend.textColor = Color.parseColor("#3D3028")
        barChart.axisRight.isEnabled = false
        barChart.axisLeft.textColor = Color.parseColor("#7A6D63")
        barChart.axisLeft.setDrawGridLines(true)

        barChart.setFitBars(true)
        barChart.animateY(1200)
        barChart.invalidate()
    }
}