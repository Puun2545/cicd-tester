from flask import Flask, request, jsonify
import aiohttp
import asyncio

app = Flask(__name__)

KEYCLOAK_SERVER = '172.17.0.3:8080'
REALM = 'test-realms'
ADMIN_USERNAME = 'admin'
ADMIN_PASSWORD = 'admin123'
CLIENT_ID = 'admin-cli'
CLIENT_SECRET = 'bXieO6lmGx4QO5zyLwoDZNL5KHIcAt80'

''' ACCESS TOKEN '''
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
            response_json = await response.text()
            if response.status == 200:
                try:
                    response_json = await response.json()
                    return response_json.get('access_token')
                except aiohttp.client_exceptions.ContentTypeError:
                    return None  # หรือทำการจัดการข้อผิดพลาดตามที่คุณต้องการ
            else:
                raise aiohttp.client_exceptions.ContentTypeError(
                    response.status, response.headers['Content-Type'], response.url
                )
            
''' GROUP MANAGEMENT '''
''' ROUTES CREATE GROUP '''
@app.route('/create-group', methods=['POST'])
async def create_group():
    token = await get_access_token()
    url = f'http://{KEYCLOAK_SERVER}/admin/realms/{REALM}/groups'
    headers = {
        'Content-Type': 'application/json',
        'Authorization': f'Bearer {token}'
    }
    data = request.get_json()
    async with aiohttp.ClientSession() as session:
        async with session.post(url, headers=headers, json=data) as response:
            response_text = await response.text()
            if response.status == 200 or response.status == 201 or response.status == 204:
                try:
                    response_json = await response.json()
                    return jsonify(response_json), response.status
                except aiohttp.client_exceptions.ContentTypeError:
                    return response_text, response.status
            else:
                return jsonify({'error': 'Unexpected content type', 'content': response_text}), response.status

''' USER MANAGEMENT '''
''' ROUTES CREATE USER '''
@app.route('/create-user', methods=['POST'])
async def create_user():
    token = await get_access_token()
    url = f'http://{KEYCLOAK_SERVER}/admin/realms/{REALM}/users'
    headers = {
        'Content-Type': 'application/json',
        'Authorization': f'Bearer {token}'
    }
    data = request.get_json()
    async with aiohttp.ClientSession() as session:
        async with session.post(url, headers=headers, json=data) as response:
            response_text = await response.text()
            if response.status == 200:
                try:
                    response_json = await response.json()
                    return jsonify(response_json), response.status
                except aiohttp.client_exceptions.ContentTypeError:
                    return response_text, response.status
            else:
                return jsonify({'error': 'Unexpected content type', 'content': response_text}), response.status

''' DELETE USER '''
@app.route('/delete-user/<user_id>', methods=['GET'])
async def delete_user(user_id):
    token = await get_access_token()
    url = f'http://{KEYCLOAK_SERVER}/admin/realms/{REALM}/users/{user_id}'
    headers = {
        'Content-Type': 'application/json',
        'Authorization': f'Bearer {token}'
    }
    async with aiohttp.ClientSession() as session:
        async with session.delete(url, headers=headers) as response:
            response_text = await response.text()
            if response.status == 200:
                try:
                    response_json = await response.json()
                    return jsonify(response_json), response.status
                except aiohttp.client_exceptions.ContentTypeError:
                    return response_text, response.status
            else:
                return jsonify({'error': 'Unexpected content type', 'content': response_text}), response.status


''' ROUTES GET USER ID '''
@app.route('/get-user-id/<user_name>', methods=['GET'])
async def get_user_id(user_name):
    token = await get_access_token()
    url = f'http://{KEYCLOAK_SERVER}/admin/realms/{REALM}/users'
    headers = {
        'Content-Type': 'application/json',
        'Authorization': f'Bearer {token}'
    }
    async with aiohttp.ClientSession() as session:
        async with session.get(url, headers=headers) as response:
            users = await response.json()
            user = next((u for u in users if u['username'] == user_name), None)
            if user:
                return jsonify({'user_id': user['id']}), 200
            return jsonify({'error': 'User not found'}), 404


''' ROUTES GET GROUP ID '''
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

        
''' ROUTES ADD USER TO GROUP '''
@app.route('/add-user-to-group/<user_name>/<group_name>', methods=['PUT'])
async def add_user_to_group(user_name, group_name):
    token = await get_access_token()
    user_id = await get_user_id(user_name)
    group_id = await get_group_id(group_name)
    url = f'http://{KEYCLOAK_SERVER}/admin/realms/{REALM}/users/{user_id}/groups/{group_id}'
    headers = {
        'Content-Type': 'application/json',
        'Authorization': f'Bearer {token}'
    }
    async with aiohttp.ClientSession() as session:
        async with session.put(url, headers=headers) as response:
            response_text = await response.text()
            if response.status == 200:
                try:
                    response_json = await response.json()
                    return jsonify(response_json), response.status
                except aiohttp.client_exceptions.ContentTypeError:
                    return response_text, response.status
            else:
                return jsonify({'error': 'Unexpected content type', 'content': response_text}), response.status


''' ROUTES CHANGE PASSWORD '''
@app.route('/change-password/<user_id>', methods=['PUT'])
async def change_password(user_id):
    token = await get_access_token()
    url = f'http://{KEYCLOAK_SERVER}/admin/realms/{REALM}/users/{user_id}/reset-password'
    headers = {
        'Content-Type': 'application/json',
        'Authorization': f'Bearer {token}'
    }
    data = request.get_json()
    async with aiohttp.ClientSession() as session:
        async with session.put(url, headers=headers, json=data) as response:
            response_text = await response.text()
            if response.status == 200:
                try:
                    response_json = await response.json()
                    return jsonify(response_json), response.status
                except aiohttp.client_exceptions.ContentTypeError:
                    return response_text, response.status
            else:
                return jsonify({'error': 'Unexpected content type', 'content': response_text}), response.status


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
            response_text = await response.text()
            if response.status == 200:
                try:
                    response_json = await response.json()
                    return jsonify(response_json), response.status
                except aiohttp.client_exceptions.ContentTypeError:
                    return response_text, response.status
            else:
                return jsonify({'error': 'Unexpected content type', 'content': response_text}), response.status


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
            response_text = await response.text()
            if response.status == 200:
                try:
                    response_json = await response.json()
                    return jsonify(response_json), response.status
                except aiohttp.client_exceptions.ContentTypeError:
                    return response_text, response.status
            else:
                return jsonify({'error': 'Unexpected content type', 'content': response_text}), response.status


@app.route('/create-client', methods=['POST'])
async def create_client():
    token = await get_access_token()
    url = f'http://{KEYCLOAK_SERVER}/admin/realms/{REALM}/clients'
    headers = {
        'Content-Type': 'application/json',
        'Authorization': f'Bearer {token}'
    }
    data = request.get_json()
    async with aiohttp.ClientSession() as session:
        async with session.post(url, headers=headers, json=data) as response:
            response_text = await response.text()
            if response.status == 200:
                try:
                    response_json = await response.json()
                    return jsonify(response_json), response.status
                except aiohttp.client_exceptions.ContentTypeError:
                    return response_text, response.status
            else:
                return jsonify({'error': 'Unexpected content type', 'content': response_text}), response.status

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
