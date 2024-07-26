from flask import Blueprint, jsonify, request, current_app
import aiohttp
from utils.keycloak import get_access_token

get_bp = Blueprint('getid', __name__)


@get_bp.route('/user/<user_name>', methods=['GET'])
async def get_user_id(user_name):
    KEYCLOAK_SERVER = current_app.config['KEYCLOAK_SERVER']
    REALM = current_app.config['REALM']
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
                return user['id'], 200
            return jsonify({'error': 'User not found'}), 404
        

@get_bp.route('/group/<group_name>', methods=['GET'])
async def get_group_id(group_name):
    token = await get_access_token()
    KEYCLOAK_SERVER = current_app.config['KEYCLOAK_SERVER']
    REALM = current_app.config['REALM']
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
                return group['id'], 200
            return jsonify({'error': 'Group not found'}), 404
