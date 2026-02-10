# Tenant Entity Statistics API

## Overview
A new REST API endpoint that returns statistics showing how many entities of each type each tenant has in the ThingsBoard platform.

## Endpoint

### GET /api/tenant/stats/entities

Returns entity count statistics for all tenants in the system.

**Authorization:** System Administrator only (`SYS_ADMIN`)

**Response:** Array of `TenantEntityStats` objects

## Response Format

```json
[
  {
    "tenantId": {
      "id": "13814000-1dd2-11b2-8080-808080808080"
    },
    "tenantName": "Tenant A",
    "entityCounts": {
      "DEVICE": 150,
      "ASSET": 45,
      "CUSTOMER": 12,
      "USER": 8,
      "DASHBOARD": 25,
      "ENTITY_VIEW": 10,
      "RULE_CHAIN": 5,
      "ALARM": 320,
      "EDGE": 3,
      "DEVICE_PROFILE": 4,
      "ASSET_PROFILE": 2
    }
  },
  {
    "tenantId": {
      "id": "23814000-1dd2-11b2-8080-808080808080"
    },
    "tenantName": "Tenant B",
    "entityCounts": {
      "DEVICE": 75,
      "ASSET": 20,
      "CUSTOMER": 5,
      "USER": 4,
      "DASHBOARD": 10,
      "ENTITY_VIEW": 5,
      "RULE_CHAIN": 3,
      "ALARM": 120,
      "EDGE": 0,
      "DEVICE_PROFILE": 2,
      "ASSET_PROFILE": 1
    }
  }
]
```

## Entity Types Tracked

The following entity types are counted for each tenant:
- DEVICE
- ASSET
- CUSTOMER
- USER
- DASHBOARD
- ENTITY_VIEW
- RULE_CHAIN
- ALARM
- EDGE
- DEVICE_PROFILE
- ASSET_PROFILE

## Implementation Details

### Files Modified/Created

1. **TenantEntityStats.java** (new)
   - Location: `common/data/src/main/java/org/thingsboard/server/common/data/TenantEntityStats.java`
   - DTO class to hold tenant statistics

2. **TenantController.java** (modified)
   - Location: `application/src/main/java/org/thingsboard/server/controller/TenantController.java`
   - Added `getTenantEntityStats()` method
   - Injected `EntityCountService` dependency

3. **TenantControllerTest.java** (modified)
   - Location: `application/src/test/java/org/thingsboard/server/controller/TenantControllerTest.java`
   - Added `testGetTenantEntityStats()` integration test

### How It Works

1. The endpoint iterates through all tenants using pagination (100 tenants per page)
2. For each tenant, it counts entities of each tracked type using `EntityCountService.countByTenantIdAndEntityType()`
3. Results are aggregated into a `TenantEntityStats` object per tenant
4. All stats are returned as a list

### Error Handling

- If counting fails for a specific entity type, the endpoint logs a warning and sets the count to 0
- The endpoint continues processing other entity types even if one fails

## Usage Example

### cURL
```bash
curl -X GET "http://localhost:8080/api/tenant/stats/entities" \
  -H "Authorization: Bearer YOUR_SYS_ADMIN_TOKEN"
```

### JavaScript
```javascript
fetch('http://localhost:8080/api/tenant/stats/entities', {
  headers: {
    'Authorization': 'Bearer YOUR_SYS_ADMIN_TOKEN'
  }
})
.then(response => response.json())
.then(stats => {
  stats.forEach(tenantStats => {
    console.log(`Tenant: ${tenantStats.tenantName}`);
    console.log(`Devices: ${tenantStats.entityCounts.DEVICE}`);
    console.log(`Assets: ${tenantStats.entityCounts.ASSET}`);
  });
});
```

## Testing

Run the integration test:
```bash
mvn test -pl application -Dtest=TenantControllerTest#testGetTenantEntityStats
```

## Security

- Endpoint is protected with `@PreAuthorize("hasAuthority('SYS_ADMIN')")`
- Only system administrators can access this endpoint
- Regular tenant admins or customer users will receive a 403 Forbidden response
