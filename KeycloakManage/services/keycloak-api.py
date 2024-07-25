from flask import Flask, request, jsonify
import aiohttp
import asyncio

app = Flask(__name__)

KEYCLOAK_SERVER = '<keycloak-server>'
REALM = 'techx-ssp-exector-cicd-tools'
ADMIN_USERNAME = 'admin'
ADMIN_PASSWORD = 'admin123'
CLIENT_ID = 'admin-cli'
CLIENT_SECRET = 'WEVBqX0rDPfxbwYsCKp8FdUUTICbG7Xq'

async def get_access_token():
    url = f'http://{KEYCLOAK_SERVER}/realms/master/protocol/openid-connect/token'
    headers = {'Content-Type': 'application/x-www-form-urlencoded'}
    data = {
        'username': ADMIN_USERNAME,
        'password': ADMIN_PASSWORD,
        'grant_type': 'client_credentials',
        'client_id': CLIENT_ID,
        'client_secret': CLIENT_SECRET
    }
    async with aiohttp.ClientSession() as session:
        async with session.post(url, headers=headers, data=data) as response:
            response_json = await response.json()
            return response_json.get('access_token')

@app.route('/create-group', methods=['POST'])
async def create_group():
    token = await get_access_token()
    url = f'http://{KEYCLOAK_SERVER}/admin/realms/{REALM}/groups'
    headers = {
        'Content-Type': 'application/json',
        'Authorization': f'Bearer {token}'
    }
    data = await request.get_json()
    async with aiohttp.ClientSession() as session:
        async with session.post(url, headers=headers, json=data) as response:
            response_json = await response.json()
            return jsonify(response_json), response.status

@app.route('/create-user', methods=['POST'])
async def create_user():
    token = await get_access_token()
    url = f'http://{KEYCLOAK_SERVER}/admin/realms/{REALM}/users'
    headers = {
        'Content-Type': 'application/json',
        'Authorization': f'Bearer {token}'
    }
    data = await request.get_json()
    async with aiohttp.ClientSession() as session:
        async with session.post(url, headers=headers, json=data) as response:
            response_json = await response.json()
            return jsonify(response_json), response.status

@app.route('/get-user-id/<username>', methods=['GET'])
async def get_user_id(username):
    token = await get_access_token()
    url = f'http://{KEYCLOAK_SERVER}/admin/realms/{REALM}/users'
    headers = {
        'Content-Type': 'application/json',
        'Authorization': f'Bearer {token}'
    }
    async with aiohttp.ClientSession() as session:
        async with session.get(url, headers=headers) as response:
            users = await response.json()
            user = next((u for u in users if u['username'] == username), None)
            if user:
                return jsonify({'user_id': user['id']}), 200
            return jsonify({'error': 'User not found'}), 404

@app.route('/get-group-id/<group_name>', methods=['GET'])
async def get_group_id(group_name):
    token = await get_access_token()
    url = f'http://{KEYCLOAK_SERVER}/admin/realms/{REALM}/groups'
    headers = {
        'Content-Type': 'application/json',
        'Authorization': f'Bearer {token}'
    }
    async with aiohttp.ClientSession() as session:
        async with session.get(url, headers=headers) as response:
            groups = await response.json()
            group = next((g for g in groups if g['name'] == group_name), None)
            if group:
                return jsonify({'group_id': group['id']}), 200
            return jsonify({'error': 'Group not found'}), 404

@app.route('/add-user-to-group/<user_id>/<group_id>', methods=['PUT'])
async def add_user_to_group(user_id, group_id):
    token = await get_access_token()
    url = f'http://{KEYCLOAK_SERVER}/admin/realms/{REALM}/users/{user_id}/groups/{group_id}'
    headers = {
        'Content-Type': 'application/json',
        'Authorization': f'Bearer {token}'
    }
    async with aiohttp.ClientSession() as session:
        async with session.put(url, headers=headers) as response:
            response_json = await response.json()
            return jsonify(response_json), response.status

@app.route('/change-password/<user_id>', methods=['PUT'])
async def change_password(user_id):
    token = await get_access_token()
    url = f'http://{KEYCLOAK_SERVER}/admin/realms/{REALM}/users/{user_id}/reset-password'
    headers = {
        'Content-Type': 'application/json',
        'Authorization': f'Bearer {token}'
    }
    data = await request.get_json()
    async with aiohttp.ClientSession() as session:
        async with session.put(url, headers=headers, json=data) as response:
            response_json = await response.json()
            return jsonify(response_json), response.status

@app.route('/remove-user-from-group/<user_id>/<group_id>', methods=['DELETE'])
async def remove_user_from_group(user_id, group_id):
    token = await get_access_token()
    url = f'http://{KEYCLOAK_SERVER}/admin/realms/{REALM}/users/{user_id}/groups/{group_id}'
    headers = {
        'Content-Type': 'application/json',
        'Authorization': f'Bearer {token}'
    }
    async with aiohttp.ClientSession() as session:
        async with session.delete(url, headers=headers) as response:
            response_json = await response.json()
            return jsonify(response_json), response.status

@app.route('/delete-group/<group_id>', methods=['DELETE'])
async def delete_group(group_id):
    token = await get_access_token()
    url = f'http://{KEYCLOAK_SERVER}/admin/realms/{REALM}/groups/{group_id}'
    headers = {
        'Content-Type': 'application/json',
        'Authorization': f'Bearer {token}'
    }
    async with aiohttp.ClientSession() as session:
        async with session.delete(url, headers=headers) as response:
            response_json = await response.json()
            return jsonify(response_json), response.status

@app.route('/create-client', methods=['POST'])
async def create_client():
    token = await get_access_token()
    url = f'http://{KEYCLOAK_SERVER}/admin/realms/{REALM}/clients'
    headers = {
        'Content-Type': 'application/json',
        'Authorization': f'Bearer {token}'
    }
    data = await request.get_json()
    async with aiohttp.ClientSession() as session:
        async with session.post(url, headers=headers, json=data) as response:
            response_json = await response.json()
            return jsonify(response_json), response.status

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
