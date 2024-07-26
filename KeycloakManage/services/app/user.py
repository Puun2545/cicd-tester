from flask import Blueprint, jsonify, request, current_app
import aiohttp
from utils.keycloak import get_access_token
from .getID import get_user_id, get_group_id

user_bp = Blueprint('user', __name__)


''' ------------------------------ USER FUNCTIONALITY ------------------------------ '''

@user_bp.route('/user', methods=['POST'])
async def create_user():
    token = await get_access_token()
    KEYCLOAK_SERVER = current_app.config['KEYCLOAK_SERVER']
    REALM = current_app.config['REALM']
    url = f'http://{KEYCLOAK_SERVER}/admin/realms/{REALM}/users'
    headers = {
        'Content-Type': 'application/json',
        'Authorization': f'Bearer {token}'
    }
    data = request.get_json()
    async with aiohttp.ClientSession() as session:
        async with session.post(url, headers=headers, json=data) as response:
            response_text = await response.text()
            if response.status in [200, 201, 204]:
                try:
                    response_json = await response.json()
                    return jsonify(response_json), response.status
                except aiohttp.client_exceptions.ContentTypeError:
                    return response_text, response.status
            return jsonify({'error': 'Unexpected content type', 'content': response_text}), response.status

@user_bp.route('/user/<user_name>', methods=['DELETE'])
async def delete_user(user_name):
    token = await get_access_token()
    KEYCLOAK_SERVER = current_app.config['KEYCLOAK_SERVER']
    REALM = current_app.config['REALM']
    user_id, _= await get_user_id(user_name)
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
            return jsonify({'error': 'Unexpected content type', 'content': response_text}), response.status

@user_bp.route('/change-password/<user_name>', methods=['PUT'])
async def change_password(user_name):
    token = await get_access_token()
    KEYCLOAK_SERVER = current_app.config['KEYCLOAK_SERVER']
    REALM = current_app.config['REALM']
    user_id, _ = await get_user_id(user_name)
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
            return jsonify({'error': 'Unexpected content type', 'content': response_text}), response.status

''' ------------------------------ USER GROUPING FUNCTIONALITY ------------------------------ '''
@user_bp.route('/grouping/<user_name>/<group_name>', methods=['PUT'])
async def add_user_to_group(user_name, group_name):
    token = await get_access_token()
    KEYCLOAK_SERVER = current_app.config['KEYCLOAK_SERVER']
    REALM = current_app.config['REALM']
    user_id, _ = await get_user_id(user_name)
    group_id, _ = await get_group_id(group_name)
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
            return jsonify({'error': 'Unexpected content type', 'content': response_text}), response.status

@user_bp.route('/grouping/<user_name>/<group_name>', methods=['DELETE'])
async def remove_user_from_group(user_name, group_name):
    token = await get_access_token()
    KEYCLOAK_SERVER = current_app.config['KEYCLOAK_SERVER']
    REALM = current_app.config['REALM']
    user_id, _ = await get_user_id(user_name)
    group_id, _ = await get_group_id(group_name)
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
            return jsonify({'error': 'Unexpected content type', 'content': response_text}), response.status
