from flask import Blueprint, jsonify, request, current_app
import aiohttp
from utils.keycloak import get_access_token
from .getID import get_group_id

group_bp = Blueprint('group', __name__)


@group_bp.route('/group', methods=['POST'])
async def create_group():
    token = await get_access_token()
    KEYCLOAK_SERVER = current_app.config['KEYCLOAK_SERVER']
    REALM = current_app.config['REALM']
    url = f'http://{KEYCLOAK_SERVER}/admin/realms/{REALM}/groups'
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

@group_bp.route('/group/<group_name>', methods=['DELETE'])
async def delete_group(group_name):
    token = await get_access_token()
    KEYCLOAK_SERVER = current_app.config['KEYCLOAK_SERVER']
    REALM = current_app.config['REALM']
    group_id = await get_group_id(group_name)
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
            return jsonify({'error': 'Unexpected content type', 'content': response_text}), response.status
