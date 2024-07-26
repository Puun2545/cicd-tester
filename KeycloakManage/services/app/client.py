from flask import Blueprint, jsonify, request, current_app
import aiohttp
from utils.keycloak import get_access_token

client_bp = Blueprint('client', __name__)

''' ------------------------------ CLIENT FUNCTIONALITY ------------------------------ '''

@client_bp.route('/create-client', methods=['POST'])
async def create_client():
    token = await get_access_token()
    KEYCLOAK_SERVER = current_app.config['KEYCLOAK_SERVER']
    REALM = current_app.config['REALM']
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
            return jsonify({'error': 'Unexpected content type', 'content': response_text}), response.status
