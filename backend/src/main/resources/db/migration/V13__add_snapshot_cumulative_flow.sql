-- Snapshots are frozen once captured. Each one records the cumulative scope contributions known at capture
-- time, so a later backdated deposit lands in the next segment's net flow instead of looking like a return.
alter table portfolio_snapshots add column scope_cumulative_flow numeric(19, 2) not null default 0;
