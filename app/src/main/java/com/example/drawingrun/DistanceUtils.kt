package com.example.drawingrun

import android.location.Location
import com.google.android.gms.maps.model.LatLng

object DistanceUtils {

    // 사용자의 위치와 경로 포인트들을 비교해 가장 가까운 거리(m)를 계산
    fun calculateMinDistanceToRoute(
        currentLocation: Location,          // 사용자 현재 위치
        routePoints: List<LatLng>           // 경로의 모든 포인트 리스트
    ): Double {
        var minDistance = Double.MAX_VALUE  // 최소 거리 초기값을 아주 큰 값으로 설정

        for (point in routePoints) {        // 경로의 각 포인트에 대해 반복
            val results = FloatArray(1)     // 결과를 담을 배열 (distanceBetween 함수 사용)

            // 현재 위치와 포인트 간의 거리를 계산해서 results[0]에 저장
            Location.distanceBetween(
                currentLocation.latitude, currentLocation.longitude,
                point.latitude, point.longitude,
                results
            )

            // 만약 이 포인트까지의 거리가 더 짧다면 minDistance 갱신
            if (results[0] < minDistance) {
                minDistance = results[0].toDouble()
            }
        }

        return minDistance // 가장 가까운 거리(m)를 반환
    }

    // 거리를 자동으로 m 또는 km 단위로 변환하여 문자열로 반환
    fun formatDistance(distanceInMeters: Double): String {
        return if (distanceInMeters < 1000) {
            // 1000m 미만이면 정수 m 단위로 표시
            "${distanceInMeters.toInt()} m"
        } else {
            // 1000m 이상이면 소수점 2자리로 km로 변환해 표시
            String.format("%.2f km", distanceInMeters / 1000.0)
        }
    }
}
