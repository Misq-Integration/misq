/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.core.trade.messages;

import bisq.common.crypto.PubKeyRing;
import bisq.core.proto.CoreProtoResolver;
import bisq.network.p2p.DirectMessage;
import bisq.network.p2p.NodeAddress;
import lombok.EqualsAndHashCode;
import lombok.Value;

@EqualsAndHashCode(callSuper = true)
@Value
public final class PrepareMultisigResponse extends TradeMessage implements DirectMessage {
    private final NodeAddress senderNodeAddress;
    private final PubKeyRing pubKeyRing;
    private final String preparedMultisigHex;
    private final long currentDate;

    public PrepareMultisigResponse(String tradeId,
                                     NodeAddress senderNodeAddress,
                                     PubKeyRing pubKeyRing,
                                     String preparedMultisigHex,
                                     String uid,
                                     int messageVersion,
                                     long currentDate) {
        super(messageVersion, tradeId, uid);
        this.senderNodeAddress = senderNodeAddress;
        this.pubKeyRing = pubKeyRing;
        this.preparedMultisigHex = preparedMultisigHex;
        this.currentDate = currentDate;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // PROTO BUFFER
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public protobuf.NetworkEnvelope toProtoNetworkEnvelope() {
        protobuf.PrepareMultisigResponse.Builder builder = protobuf.PrepareMultisigResponse.newBuilder()
                .setTradeId(tradeId)
                .setSenderNodeAddress(senderNodeAddress.toProtoMessage())
                .setPubKeyRing(pubKeyRing.toProtoMessage())
                .setPreparedMultisigHex(preparedMultisigHex)
                .setUid(uid);

        builder.setCurrentDate(currentDate);

        return getNetworkEnvelopeBuilder().setPrepareMultisigResponse(builder).build();
    }

    public static PrepareMultisigResponse fromProto(protobuf.PrepareMultisigResponse proto,
                                                      CoreProtoResolver coreProtoResolver,
                                                      int messageVersion) {
        return new PrepareMultisigResponse(proto.getTradeId(),
                NodeAddress.fromProto(proto.getSenderNodeAddress()),
                PubKeyRing.fromProto(proto.getPubKeyRing()),
                proto.getPreparedMultisigHex(),
                proto.getUid(),
                messageVersion,
                proto.getCurrentDate());
    }

    @Override
    public String toString() {
        return "PrepareMultisigResponse{" +
                "\n     senderNodeAddress=" + senderNodeAddress +
                ",\n     pubKeyRing=" + pubKeyRing +
                ",\n     preparedMultisigHex='" + preparedMultisigHex + '\'' +
                ",\n     currentDate=" + currentDate +
                "\n} " + super.toString();
    }
}
