void calculatePositions(int latitudinalSize, int longitudinalSize, constant float* altitudes, global float* positions, int lat, int lng);
void calculateNormals(int latitudinalSize, int longitudinalSize, global float* positions, global float* normals, int lat, int lng);
void loadPositionVertexBuffer(int latitudinalSize, int longitudinalSize, global float* positions, global float* positionVertexBuffer, int lat, int lng);
float getAltitude(int latitudinalSize, int longitudinalSize, constant float* altitudes, int lat, int lng);
float3 getPosition(int latitudinalSize, int longitudinalSize, global float* positions, int lat, int lng);

kernel void calculatePositionsAndNormals(int latitudinalSize, int longitudinalSize, constant float* altitudes, global float* positions, global float* normals) {
	int lat = get_global_id(LATITUDE);
	int lng = get_global_id(LONGITUDE);
	if ((lat < 2 * latitudinalSize + 1) && (lng < 2 * longitudinalSize + 1)) {
		calculatePositions(latitudinalSize, longitudinalSize, altitudes, positions, lat, lng);
		mem_fence(CLK_GLOBAL_MEM_FENCE);
		calculateNormals(latitudinalSize, longitudinalSize, positions, normals, lat, lng);
	}
}

kernel void loadVertexBuffers(int latitudinalSize, int longitudinalSize, global float* positions, global float* positionVertexBuffer) {
	int lat = get_global_id(LATITUDE);
	int lng = get_global_id(LONGITUDE);
	if ((lat < latitudinalSize) && (lng < longitudinalSize)) {
		loadPositionVertexBuffer(latitudinalSize, longitudinalSize, positions, positionVertexBuffer, lat, lng);
	}
}

void calculatePositions(int latitudinalSize, int longitudinalSize, constant float* altitudes, global float* positions, int lat, int lng) {
	int latitude = (lat - 1) / 2;
	int latitudeNorth = lat / 2;
	int latitudeSouth = lat / 2 - 1;
	int longitude = (lng - 1) / 2;
	int longitudeEast = lng / 2;
	int longitudeWest = lng / 2 - 1;
	float altitude = getAltitude(latitudinalSize, longitudinalSize, altitudes, latitude, longitude);
	float altitudeNorth = getAltitude(latitudinalSize, longitudinalSize, altitudes, latitudeNorth, longitude);
	float altitudeNortheast = getAltitude(latitudinalSize, longitudinalSize, altitudes, latitudeNorth, longitudeEast);
	float altitudeEast = getAltitude(latitudinalSize, longitudinalSize, altitudes, latitude, longitudeEast);
	float altitudeSoutheast = getAltitude(latitudinalSize, longitudinalSize, altitudes, latitudeSouth, longitudeEast);
	float altitudeSouth = getAltitude(latitudinalSize, longitudinalSize, altitudes, latitudeSouth, longitude);
	float altitudeSouthwest = getAltitude(latitudinalSize, longitudinalSize, altitudes, latitudeSouth, longitudeWest);
	float altitudeWest = getAltitude(latitudinalSize, longitudinalSize, altitudes, latitude, longitudeWest);
	float altitudeNorthwest = getAltitude(latitudinalSize, longitudinalSize, altitudes, latitudeNorth, longitudeWest);
	float3 centerPosition = (float3) (longitude + 0.5f, altitude, -latitude - 0.5f);
	float3 latitudinalPosition = (float3) (longitude + 0.5f, (altitudeNorth + altitudeSouth) / 2.0f, -latitudeNorth);
	float3 longitudinalPosition = (float3) (longitudeEast, (altitudeEast + altitudeWest) / 2.0f, -latitude - 0.5f);
	float3 latitudinalLongitudinalPosition = (float3) (longitudeEast, (altitudeNortheast + altitudeSoutheast + altitudeSouthwest + altitudeNorthwest) / 4.0f, -latitudeNorth);
	int vertex = lat * (2 * longitudinalSize + 1) + lng;
	if ((lat % 2 == 0) && (lng % 2 == 0)) { // vertex between four tiles both latitudinally and longitudinally
		vstore3(latitudinalLongitudinalPosition, vertex, positions);
	} else if (lat % 2 == 0) { // vertex between two tiles latitudinally
		vstore3(latitudinalPosition, vertex, positions);
	} else if (lng % 2 == 0) { // vertex between two tiles longitudinally
		vstore3(longitudinalPosition, vertex, positions);
	} else { // vertex in the center of the tile
		vstore3(centerPosition, vertex, positions);
	}
}

void calculateNormals(int latitudinalSize, int longitudinalSize, global float* positions, global float* normals, int lat, int lng) {
	float3 position = getPosition(latitudinalSize, longitudinalSize, positions, lat, lng);
	float3 positionNorth = getPosition(latitudinalSize, longitudinalSize, positions, lat + 1, lng);
	float3 positionNortheast = getPosition(latitudinalSize, longitudinalSize, positions, lat + 1, lng + 1);
	float3 positionEast = getPosition(latitudinalSize, longitudinalSize, positions, lat, lng + 1);
	float3 positionSoutheast = getPosition(latitudinalSize, longitudinalSize, positions, lat -1, lng + 1);
	float3 positionSouth = getPosition(latitudinalSize, longitudinalSize, positions, lat - 1, lng);
	float3 positionSouthwest = getPosition(latitudinalSize, longitudinalSize, positions, lat - 1, lng - 1);
	float3 positionWest = getPosition(latitudinalSize, longitudinalSize, positions, lat, lng - 1);
	float3 positionNorthwest = getPosition(latitudinalSize, longitudinalSize, positions, lat + 1, lng -1);
	int vertex = lat * (2 * longitudinalSize + 1) + lng;
	float3 normalNorthNortheast = normalize(cross(positionNortheast - position, positionNorth - position));
	float3 normalNortheast = normalize(cross(positionEast - position, positionNorth - position));
	float3 normalNortheastEast = normalize(cross(positionEast - position, positionNortheast - position));
	float3 normalEastSoutheast = normalize(cross(positionSoutheast - position, positionEast - position));
	float3 normalSoutheast = normalize(cross(positionSouth - position, positionEast - position));
	float3 normalSoutheastSouth = normalize(cross(positionSouth - position, positionSoutheast - position));
	float3 normalSouthSouthwest = normalize(cross(positionSouthwest - position, positionSouth - position));
	float3 normalSouthwest = normalize(cross(positionWest - position, positionSouth - position));
	float3 normalSouthwestWest = normalize(cross(positionWest - position, positionSouthwest - position));
	float3 normalWestNorthwest = normalize(cross(positionNorthwest - position, positionWest - position));
	float3 normalNorthwest = normalize(cross(positionNorth - position, positionWest - position));
	float3 normalNorthwestNorth = normalize(cross(positionNorth - position, positionNorthwest - position));
	float3 normal4 = normalize((normalNortheast + normalSoutheast + normalSouthwest + normalNorthwest) / 4.0f); // normal between four faces
	float3 normal8 = normalize((normalNorthNortheast + normalNortheastEast + normalEastSoutheast + normalSoutheastSouth + normalSouthSouthwest + normalSouthwestWest + normalWestNorthwest + normalNorthwestNorth) / 8.0f); // normal between eight faces
	vstore3(((lat % 2 == 0) != (lng % 2 == 0)) ? normal4 : normal8, vertex, normals);
}

void loadPositionVertexBuffer(int latitudinalSize, int longitudinalSize, global float* positions, global float* positionVertexBuffer, int lat, int lng) {
	int latitude = lat * 2 + 1;
	int latitudeNorth = (lat + 1) * 2;
	int latitudeSouth = lat * 2;
	int longitude = lng * 2 + 1;
	int longitudeEast = (lng + 1) * 2;
	int longitudeWest = lng * 2;
	float3 positionCenter = getPosition(latitudinalSize, longitudinalSize, positions, latitude, longitude);
	float3 positionNorth = getPosition(latitudinalSize, longitudinalSize, positions, latitudeNorth, longitude);
	float3 positionNortheast = getPosition(latitudinalSize, longitudinalSize, positions, latitudeNorth, longitudeEast);
	float3 positionEast = getPosition(latitudinalSize, longitudinalSize, positions, latitude, longitudeEast);
	float3 positionSoutheast = getPosition(latitudinalSize, longitudinalSize, positions, latitudeSouth, longitudeEast);
	float3 positionSouth = getPosition(latitudinalSize, longitudinalSize, positions, latitudeSouth, longitude);
	float3 positionSouthwest = getPosition(latitudinalSize, longitudinalSize, positions, latitudeSouth, longitudeWest);
	float3 positionWest = getPosition(latitudinalSize, longitudinalSize, positions, latitude, longitudeWest);
	float3 positionNorthwest = getPosition(latitudinalSize, longitudinalSize, positions, latitudeNorth, longitudeWest);
	int offset = lat * longitudinalSize * DIRECTIONS * TRIANGLE_VERTICES + lng * DIRECTIONS * TRIANGLE_VERTICES;
	vstore3(positionNorth, offset + NORTH * TRIANGLE_VERTICES + V1, positionVertexBuffer);
	vstore3(positionCenter, offset + NORTH * TRIANGLE_VERTICES + V2, positionVertexBuffer);
	vstore3(positionNortheast, offset + NORTH * TRIANGLE_VERTICES + V3, positionVertexBuffer);
	vstore3(positionNortheast, offset + NORTHEAST * TRIANGLE_VERTICES + V1, positionVertexBuffer);
	vstore3(positionCenter, offset + NORTHEAST * TRIANGLE_VERTICES + V2, positionVertexBuffer);
	vstore3(positionEast, offset + NORTHEAST * TRIANGLE_VERTICES + V3, positionVertexBuffer);
	vstore3(positionEast, offset + EAST * TRIANGLE_VERTICES + V1, positionVertexBuffer);
	vstore3(positionCenter, offset + EAST * TRIANGLE_VERTICES + V2, positionVertexBuffer);
	vstore3(positionSoutheast, offset + EAST * TRIANGLE_VERTICES + V3, positionVertexBuffer);
	vstore3(positionSoutheast, offset + SOUTHEAST * TRIANGLE_VERTICES + V1, positionVertexBuffer);
	vstore3(positionCenter, offset + SOUTHEAST * TRIANGLE_VERTICES + V2, positionVertexBuffer);
	vstore3(positionSouth, offset + SOUTHEAST * TRIANGLE_VERTICES + V3, positionVertexBuffer);
	vstore3(positionSouth, offset + SOUTH * TRIANGLE_VERTICES + V1, positionVertexBuffer);
	vstore3(positionCenter, offset + SOUTH * TRIANGLE_VERTICES + V2, positionVertexBuffer);
	vstore3(positionSouthwest, offset + SOUTH * TRIANGLE_VERTICES + V3, positionVertexBuffer);
	vstore3(positionSouthwest, offset + SOUTHWEST * TRIANGLE_VERTICES + V1, positionVertexBuffer);
	vstore3(positionCenter, offset + SOUTHWEST * TRIANGLE_VERTICES + V2, positionVertexBuffer);
	vstore3(positionWest, offset + SOUTHWEST * TRIANGLE_VERTICES + V3, positionVertexBuffer);
	vstore3(positionWest, offset + WEST * TRIANGLE_VERTICES + V1, positionVertexBuffer);
	vstore3(positionCenter, offset + WEST * TRIANGLE_VERTICES + V2, positionVertexBuffer);
	vstore3(positionNorthwest, offset + WEST * TRIANGLE_VERTICES + V3, positionVertexBuffer);
	vstore3(positionNorthwest, offset + NORTHWEST * TRIANGLE_VERTICES + V1, positionVertexBuffer);
	vstore3(positionCenter, offset + NORTHWEST * TRIANGLE_VERTICES + V2, positionVertexBuffer);
	vstore3(positionNorth, offset + NORTHWEST * TRIANGLE_VERTICES + V3, positionVertexBuffer);
}

float getAltitude(int latitudinalSize, int longitudinalSize, constant float* altitudes, int lat, int lng) {
	lat = (lat < 0) ? 0 : ((lat < latitudinalSize) ? lat : (latitudinalSize - 1));
	lng = (lng < 0) ? 0 : ((lng < longitudinalSize) ? lng : (longitudinalSize - 1));
	return altitudes[lat * longitudinalSize + lng];
}

float3 getPosition(int latitudinalSize, int longitudinalSize, global float* positions, int lat, int lng) {
	lat = (lat < 0) ? 0 : ((lat < 2 * latitudinalSize + 1) ? lat : (2 * latitudinalSize));
	lng = (lng < 0) ? 0 : ((lng < 2 * longitudinalSize + 1) ? lng : (2 * longitudinalSize));
	return vload3(lat * (longitudinalSize * 2 + 1) + lng, positions);
}
