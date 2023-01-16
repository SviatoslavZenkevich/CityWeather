package com.example.cityweather.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.cityweather.MainViewModel
import com.example.cityweather.WeatherModel
import com.example.cityweather.adapter.WeatherAdapter
import com.example.cityweather.databinding.FragmentMainBinding
import com.squareup.picasso.Picasso
import org.json.JSONObject

const val API_KEY = "db9a768016c3403cb03162546231501"


class MainFragment : Fragment() {
    private lateinit var adapter: WeatherAdapter
    private val model: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model.liveDataList.observe(viewLifecycleOwner) {
            adapter.submitList(it.subList(1, it.size))
        }
        requestWeatherData("Berlin")
    }

    private fun updateCurrentCard() = with(binding) {
        model.liveDataCurrent.observe(viewLifecycleOwner) {
            tvCity.text = it.city
            tvDate.text = it.date
            tvCondition.text = it.condition
            Picasso.get().load("https:" + it.imageUrl).into(imWeather)
            tvCurrentTemp.text = it.currentTemp
        }
    }

    private fun requestWeatherData(city: String) {
        val url = "https://api.weatherapi.com/v1/forecast.json?key=" +
                API_KEY +
                "&q=" +
                city +
                "&days=" +
                "3" +
                "&aqi=no&alerts=no"
        val queue = Volley.newRequestQueue(context)
        val request = StringRequest(
            Request.Method.GET,
            url,
            { result ->
                parseWeatherData(result)
            },
            { error ->
                Log.d("MyLog", "Error: $error")
            }
        )
        queue.add(request)
    }

    private fun parseWeatherData(result: String) {
        val mainObject = JSONObject(result)
        val list = parseDays(mainObject)
        parseCurrentData(mainObject, list[0])
    }

    private fun parseDays(mainObject: JSONObject): List<WeatherModel> {
        val list = ArrayList<WeatherModel>()
        val daysArray = mainObject.getJSONObject("forecast")
            .getJSONArray("forecastday")
        val name = mainObject.getJSONObject("location").getString("name")
        for (i in 0 until 4) {
            val day = daysArray[i] as JSONObject
            val item = WeatherModel(
                name,
                day.getString("date"),
                day.getJSONObject("day").getJSONObject("condition")
                    .getString("text"),
                day.getJSONObject("day").getJSONObject("condition")
                    .getString("icon"),
                "",
                day.getJSONObject("day").getString("maxtemp_c").toFloat().toInt().toString(),
            )
            list.add(item)
        }
        model.liveDataList.value = list
        return list
    }

    private fun parseCurrentData(mainObject: JSONObject, weatherItem: WeatherModel) {
        val item = WeatherModel(
            mainObject.getJSONObject("location").getString("name"),
            mainObject.getJSONObject("current").getString("last_updated"),
            mainObject.getJSONObject("current")
                .getJSONObject("condition").getString("text"),
            mainObject.getJSONObject("current")
                .getJSONObject("condition").getString("icon"),
            mainObject.getJSONObject("current").getString("temp_c"),
            ""
        )

        model.liveDataCurrent.value = item
    }


    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}


